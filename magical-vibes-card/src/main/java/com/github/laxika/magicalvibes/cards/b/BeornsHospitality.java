package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "120")
public class BeornsHospitality extends Card {

    public BeornsHospitality() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(
                        new AnimatePermanentsEffect(null, null, List.of(CardSubtype.BEAR), Set.of(), null,
                                Set.of(), GrantScope.SELF, EffectDuration.PERMANENT, null),
                        new GrantStaticEffectToSourceEffect(
                                new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl),
                                EffectDuration.PERMANENT)),
                "{5}{G}{G}: This enchantment becomes a Bear creature in addition to its other types and gains "
                        + "\"This creature's power and toughness are each equal to the number of lands you control.\" "
                        + "(This effect doesn't end.)"));
    }
}
