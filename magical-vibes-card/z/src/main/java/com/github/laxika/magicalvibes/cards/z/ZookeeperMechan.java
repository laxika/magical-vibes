package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creatureYouControl;

@CardRegistration(set = "EOE", collectorNumber = "170")
public class ZookeeperMechan extends Card {

    public ZookeeperMechan() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}",
                List.of(new BoostTargetCreatureEffect(4, 0)),
                "{6}{R}: Target creature you control gets +4/+0 until end of turn. Activate only as a sorcery.",
                creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
