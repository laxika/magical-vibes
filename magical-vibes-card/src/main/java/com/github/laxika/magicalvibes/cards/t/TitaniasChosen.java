package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "277")
public class TitaniasChosen extends Card {

    public TitaniasChosen() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.GREEN), 1, false));
    }
}
