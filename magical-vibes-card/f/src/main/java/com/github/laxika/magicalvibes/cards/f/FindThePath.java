package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "183")
public class FindThePath extends Card {

    public FindThePath() {
        target(TargetFilters.land())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new VentureIntoDungeonEffect())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null, List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                                "{T}: Add {G}{G}."),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
