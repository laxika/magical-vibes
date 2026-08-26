package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "141")
public class RhinosRampage extends Card {

    public RhinosRampage() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 0));
        var fightTarget = target(TargetFilters.creatureAnOpponentControls());
        fightTarget.addEffect(EffectSlot.SPELL, new FightTargetsEffect());

        PermanentPredicate artifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentMaxManaValuePredicate(3)));
        fightTarget.addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new EventValueAtLeast(1),
                new QueueReflexiveAbilityEffect(new DestroyTargetPermanentEffect(artifact), true)));
    }
}
