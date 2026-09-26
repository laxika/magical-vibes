package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1558")
public class KelsienThePlague extends Card {

    public KelsienThePlague() {
        // Kelsien gets +1/+1 for each experience counter you have.
        ControllerExperienceCounters experienceCounters = new ControllerExperienceCounters();
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(experienceCounters, experienceCounters));

        // {T}: Kelsien deals 1 damage to target creature an opponent controls. When that creature
        // dies this turn, you get an experience counter.
        PermanentNotPredicate creatureOpponentControls =
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DealDamageToTargetCreatureEffect(1, creatureOpponentControls),
                        new ResolveEffectOnTargetDeathThisTurnEffect(new ExperienceCountersEffect(1))
                ),
                "{T}: Kelsien, the Plague deals 1 damage to target creature an opponent controls. "
                        + "When that creature dies this turn, you get an experience counter.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
