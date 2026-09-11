package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchHandAndOrLibraryForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "103")
public class LastLightOfDurinsDay extends Card {

    public LastLightOfDurinsDay() {
        CardEffect summonDragon = new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.QUEST),
                new SacrificeSelfThenEffect(
                        new SearchHandAndOrLibraryForCardToBattlefieldEffect(
                                new CardSubtypePredicate(CardSubtype.DRAGON))));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.QUEST),
                                summonDragon)));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                "Mountaincycling {2} ({2}, Discard this card: Search your library for a Mountain card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
