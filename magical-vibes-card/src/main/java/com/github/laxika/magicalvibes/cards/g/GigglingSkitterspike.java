package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "39")
@CardRegistration(set = "DSC", collectorNumber = "66")
public class GigglingSkitterspike extends Card {

    public GigglingSkitterspike() {
        addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_BLOCK,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new MonstrosityEffect(5)),
                "{5}: Monstrosity 5."
        ).withActivationCondition(new NotCondition(new SourceIsMonstrous()),
                "This creature is already monstrous"));
    }
}
