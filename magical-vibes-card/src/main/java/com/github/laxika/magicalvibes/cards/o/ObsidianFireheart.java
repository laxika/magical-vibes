package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "140")
public class ObsidianFireheart extends Card {

    public ObsidianFireheart() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.BLAZE, 1),
                        new GrantEffectToTargetEffect(
                                EffectSlot.UPKEEP_TRIGGERED,
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))
                ),
                "{1}{R}{R}: Put a blaze counter on target land without a blaze counter on it. For as long as that land has a blaze counter on it, it has \"At the beginning of your upkeep, this land deals 1 damage to you.\"",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.BLAZE))
                        )),
                        "Target must be a land without a blaze counter on it"
                )
        ));
    }
}
