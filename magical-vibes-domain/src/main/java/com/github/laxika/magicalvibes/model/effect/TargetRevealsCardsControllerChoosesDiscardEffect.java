package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player reveals {@code revealCount} cards from their hand and you choose one of them.
 * That player discards that card." (Blackmail).
 *
 * <p>Unlike the Duress family ({@link ChooseCardsFromTargetHandEffect}), the target player picks
 * which cards to reveal — only those cards are shown to the controller, who then chooses one for
 * the target to discard. If the target has {@code revealCount} or fewer cards, their whole hand is
 * revealed with no choice.
 */
public record TargetRevealsCardsControllerChoosesDiscardEffect(int revealCount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
