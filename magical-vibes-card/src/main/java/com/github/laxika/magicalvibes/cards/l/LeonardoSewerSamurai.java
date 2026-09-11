package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "17")
@CardRegistration(set = "TMT", collectorNumber = "215")
@CardRegistration(set = "TMT", collectorNumber = "301")
public class LeonardoSewerSamurai extends Card {

    public LeonardoSewerSamurai() {
        addSneak("{2}{W}{W}");

        CardPredicate lowPowerOrToughness = new CardAnyOfPredicate(List.of(
                new CardPowerAtMostPredicate(1),
                new CardNotPredicate(new CardToughnessAtLeastPredicate(2))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new CastSpellsFromGraveyardEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                lowPowerOrToughness)),
                        List.of(), CounterType.FINALITY)));
    }
}
