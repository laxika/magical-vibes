package com.github.laxika.magicalvibes.cards.r;



import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "142")

public class ResoluteSurvivors extends Card {

    public ResoluteSurvivors() {
        addStaticEffect(new ExertAsItAttacksEffect());

        addTrigger(Triggers.youExertCreature(), SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));
    }
}
