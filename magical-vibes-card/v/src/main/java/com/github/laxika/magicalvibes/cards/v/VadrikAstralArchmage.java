package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "248")
@CardRegistration(set = "DBL", collectorNumber = "248")
public class VadrikAstralArchmage extends Card {

    public VadrikAstralArchmage() {
        addStaticEffect(new BecomeDayAsEntersEffect());

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                instantOrSorcery, new SourcePower(), CostModificationScope.SELF));

        addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
