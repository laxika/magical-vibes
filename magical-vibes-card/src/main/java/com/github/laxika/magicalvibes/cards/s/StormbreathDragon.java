package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "143")
public class StormbreathDragon extends Card {

    public StormbreathDragon() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}{R}",
                List.of(new MonstrosityEffect(3)),
                "{5}{R}{R}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                new DealDamageToEachOpponentEqualToCardsInHandEffect());
    }
}
