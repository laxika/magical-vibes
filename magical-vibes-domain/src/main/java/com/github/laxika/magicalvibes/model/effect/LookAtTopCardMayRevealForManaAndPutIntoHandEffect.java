package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;

/**
 * Looks at the controller's top card. If it has enough colored mana symbols, the controller may
 * reveal it; accepting adds three mana chosen from that card's colors and puts it into the hand.
 * Declining, or an ineligible card, puts the card into the hand without revealing it.
 */
public record LookAtTopCardMayRevealForManaAndPutIntoHandEffect(
        Stage stage,
        List<ManaColor> colors
) implements CardEffect {

    public LookAtTopCardMayRevealForManaAndPutIntoHandEffect {
        colors = List.copyOf(colors);
    }

    public enum Stage {
        LOOK,
        MAY_REVEAL
    }

    public LookAtTopCardMayRevealForManaAndPutIntoHandEffect() {
        this(Stage.LOOK, List.of());
    }

    public LookAtTopCardMayRevealForManaAndPutIntoHandEffect withMayRevealStage(List<ManaColor> colors) {
        return new LookAtTopCardMayRevealForManaAndPutIntoHandEffect(Stage.MAY_REVEAL, colors);
    }
}
