package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "248")
public class VadrikAstralArchmage extends Card {

    public VadrikAstralArchmage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                instantOrSorcery, new SourcePower(), CostModificationScope.SELF));

        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
