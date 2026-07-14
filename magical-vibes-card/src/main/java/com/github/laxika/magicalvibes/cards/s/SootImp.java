package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "EVE", collectorNumber = "43")
public class SootImp extends Card {

    public SootImp() {
        // Flying is auto-loaded as a keyword from Scryfall.

        // Whenever a player casts a nonblack spell, that player loses 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CasterLosesLifeOnSpellCastEffect(
                        new CardNotPredicate(new CardColorPredicate(CardColor.BLACK)), 1));
    }
}
