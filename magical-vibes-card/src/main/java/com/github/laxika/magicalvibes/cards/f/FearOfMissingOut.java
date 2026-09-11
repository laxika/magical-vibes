package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "136")
public class FearOfMissingOut extends Card {

    public FearOfMissingOut() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardAndDrawCardEffect());
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(
                new ConditionalEffect(new Delirium(), SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new AdditionalCombatPhaseEffect(1)))));
    }
}
