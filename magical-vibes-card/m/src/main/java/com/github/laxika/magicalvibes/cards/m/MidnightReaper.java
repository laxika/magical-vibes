package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "77")
@CardRegistration(set = "SLD", collectorNumber = "1171")
@CardRegistration(set = "RVR", collectorNumber = "86")
@CardRegistration(set = "MOC", collectorNumber = "257")
@CardRegistration(set = "MIC", collectorNumber = "125")
@CardRegistration(set = "DRC", collectorNumber = "44")
@CardRegistration(set = "SCD", collectorNumber = "91")
@CardRegistration(set = "FDC", collectorNumber = "114")
public class MidnightReaper extends Card {

    public MidnightReaper() {
        SequenceEffect deathEffect = SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                new DrawCardEffect(1));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, deathEffect);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new SourceIsToken()), deathEffect));
    }
}
