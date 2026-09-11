package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "87")
public class AcererakTheArchlich extends Card {

    public AcererakTheArchlich() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new NotCondition(new ControllerHasCompletedDungeon()),
                SequenceEffect.of(ReturnToHandEffect.self(), new VentureIntoDungeonEffect())));
        addEffect(EffectSlot.ON_ATTACK,
                new EachOpponentCreatesTokenUnlessSacrificesCreatureEffect(CreateTokenEffect.blackZombie(1)));
    }
}
