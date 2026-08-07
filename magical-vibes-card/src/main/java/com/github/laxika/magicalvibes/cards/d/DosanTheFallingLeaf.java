package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCanCastSpellsOnlyDuringOwnTurnEffect;

/**
 * Dosan the Falling Leaf: players can cast spells only during their own turns. Activated
 * abilities are unaffected.
 */
@CardRegistration(set = "CHK", collectorNumber = "205")
public class DosanTheFallingLeaf extends Card {

    public DosanTheFallingLeaf() {
        addEffect(EffectSlot.STATIC, new PlayersCanCastSpellsOnlyDuringOwnTurnEffect());
    }
}
