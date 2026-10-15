package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "503")
@CardRegistration(set = "LTC", collectorNumber = "547")
public class OlRinsSearingLight extends Card {

    public OlRinsSearingLight() {
        addEffect(EffectSlot.SPELL,
                new EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect(
                        new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))));
    }
}
