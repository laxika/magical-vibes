package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "5")
public class KotisSibsigChampion extends Card {

    public KotisSibsigChampion() {
        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(), 3));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                new EnteringCreatureFromGraveyardConditionalEffect(
                        new PutCountersOnSourceEffect(1, 1, 2)));
    }
}
