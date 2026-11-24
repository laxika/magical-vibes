package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * Dosan the Falling Leaf: players can cast spells only during their own turns. Activated
 * abilities are unaffected.
 */
@CardRegistration(set = "CHK", collectorNumber = "205")
@CardRegistration(set = "SLD", collectorNumber = "2404")
public class DosanTheFallingLeaf extends Card {

    public DosanTheFallingLeaf() {
        addStaticEffect(OpponentsCantCastOrActivateDuringYourTurnEffect.onlyDuringTheirOwnTurns(Subjects.eachPlayer(), null));
    }
}
