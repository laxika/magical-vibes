package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "198")
public class WelcomeToSweettooth extends Card {

    public WelcomeToSweettooth() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Human", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(2), foodToken());

        PermanentCount foodsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOOD), CountScope.CONTROLLER);
        addTrigger(Triggers.chapter(3), new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new Sum(new Fixed(1), foodsYouControl)));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(3);
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
