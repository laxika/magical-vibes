package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "PIP", collectorNumber = "110")
@CardRegistration(set = "PIP", collectorNumber = "420")
@CardRegistration(set = "PIP", collectorNumber = "638")
@CardRegistration(set = "PIP", collectorNumber = "948")
public class MoiraBrownGuideAuthor extends Card {

    public MoiraBrownGuideAuthor() {
        PermanentCounterSum questCountersAmongPermanentsYouControl = new PermanentCounterSum(
                CounterType.QUEST,
                new PermanentTruePredicate(),
                CountScope.CONTROLLER);
        CreateTokenEffect wastelandSurvivalGuide = CreateTokenEffect.ofArtifactToken(
                        1,
                        "Wasteland Survival Guide",
                        List.of(CardSubtype.EQUIPMENT),
                        List.of(new EquipActivatedAbility("{1}")))
                .withTokenEffects(Map.of(
                        EffectSlot.STATIC,
                        new AttachedBoostEffect(
                                questCountersAmongPermanentsYouControl,
                                questCountersAmongPermanentsYouControl,
                                GrantScope.EQUIPPED_CREATURE)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, wastelandSurvivalGuide);

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent you control"))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.QUEST));
    }
}
