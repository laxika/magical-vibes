package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "87")
@CardRegistration(set = "SLD", collectorNumber = "1784")
public class AcererakTheArchlich extends Card {

    public AcererakTheArchlich() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new ControllerHasCompletedDungeon(com.github.laxika.magicalvibes.model.Dungeon.TOMB_OF_ANNIHILATION)),
                SequenceEffect.of(ReturnToHandEffect.self(), new VentureIntoDungeonEffect())));
        addTrigger(Triggers.attacks(),
                new EachOpponentCreatesTokenUnlessSacrificesCreatureEffect(CreateTokenEffect.blackZombie(1)));
    }
}
