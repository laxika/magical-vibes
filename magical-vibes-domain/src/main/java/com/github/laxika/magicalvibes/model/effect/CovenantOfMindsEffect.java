package com.github.laxika.magicalvibes.model.effect;

/**
 * Covenant of Minds. Reveal the top three cards of your library. Target opponent may choose to put
 * those cards into your hand. If they don't, put those cards into your graveyard and draw five
 * cards. The revealed cards stay on top of the library while the targeted opponent decides via the
 * may-ability (accept/decline) system: accept puts the revealed cards into the controller's hand,
 * decline mills them and draws five.
 */
public record CovenantOfMindsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
