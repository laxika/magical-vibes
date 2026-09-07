package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "197")
public class RideTheShoopuf extends Card {

    public RideTheShoopuf() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new AnimatePermanentsEffect(
                        7, 7, List.of(CardSubtype.BEAST), Set.of(), null,
                        Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.PERMANENT)),
                "{5}{G}{G}: This enchantment becomes a 7/7 Beast creature in addition to its other types."
        ));
    }
}
