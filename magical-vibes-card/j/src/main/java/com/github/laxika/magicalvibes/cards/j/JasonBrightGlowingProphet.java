package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerDifferentFromBasePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "32")
@CardRegistration(set = "PIP", collectorNumber = "376")
@CardRegistration(set = "PIP", collectorNumber = "560")
@CardRegistration(set = "PIP", collectorNumber = "904")
public class JasonBrightGlowingProphet extends Card {

    public JasonBrightGlowingProphet() {
        PermanentPredicate modifiedZombieOrMutant = new PermanentAllOfPredicate(List.of(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT)),
                new PermanentPowerDifferentFromBasePowerPredicate()));
        TriggeringPermanentConditionalEffect drawTrigger = new TriggeringPermanentConditionalEffect(
                modifiedZombieOrMutant, new DrawCardEffect(1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, drawTrigger);
        addEffect(EffectSlot.ON_DEATH, drawTrigger);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                ),
                "Come Fly With Me — {2}, Sacrifice a creature: Put a +1/+1 counter on target creature you control. "
                        + "It gains flying until end of turn.",
                TargetFilters.creatureYouControl()));
    }
}
