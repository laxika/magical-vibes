package com.github.laxika.magicalvibes.model.effect;

/**
 * Target opponent exiles cards from the top of their library until they exile a nonland card.
 * Until end of turn, the source's controller may cast that nonland card without paying its mana
 * cost (Nicol Bolas, God-Pharaoh +2). Intermediate land cards stay exiled with no play permission.
 * The cast window expires at end of turn and does not depend on the source remaining on the
 * battlefield.
 */
public record ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
