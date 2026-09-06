package de.distantnova.audioperspective.network;

import de.distantnova.audioperspective.AudioPerspective;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public final class VirtualListenerPayload {

    private VirtualListenerPayload() {
    }

    public record Update(Identifier dimension, double x, double y, double z) implements CustomPacketPayload {

        public static final Type<Update> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(AudioPerspective.MOD_ID, "update_listener")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, Update> CODEC = CustomPacketPayload.codec(
                Update::write,
                Update::new
        );

        private Update(RegistryFriendlyByteBuf buffer) {
            this(Identifier.STREAM_CODEC.decode(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        private void write(RegistryFriendlyByteBuf buffer) {
            Identifier.STREAM_CODEC.encode(buffer, dimension);
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
        }

        @Override
        @NotNull
        public Type<Update> type() {
            return TYPE;
        }
    }

    public record Clear() implements CustomPacketPayload {

        public static final Clear INSTANCE = new Clear();
        public static final Type<Clear> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(AudioPerspective.MOD_ID, "clear_listener")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, Clear> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        @NotNull
        public Type<Clear> type() {
            return TYPE;
        }
    }
}
