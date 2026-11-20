package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "106")
@CardRegistration(set = "V17", collectorNumber = "14")
@CardRegistration(set = "PIO", collectorNumber = "96")
@CardRegistration(set = "CC2", collectorNumber = "1")
public class LilianaHereticalHealer extends Card {

    public LilianaHereticalHealer() {
        setBackFaceCard(new LilianaDefiantNecromancer());

        // Whenever another nontoken creature you control dies, exile Liliana, Heretical Healer, then
        // return her to the battlefield transformed under her owner's control. If you do, create a
        // 2/2 black Zombie creature token.
        // If Liliana has already left the battlefield when the trigger resolves, nothing is exiled and
        // no Zombie is created.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new ExileSelfAndReturnTransformedEffect(),
                        new ConditionalEffect(HappenedThisWay.returned(), CreateTokenEffect.blackZombie(1))));
    }

    @Override
    public String getBackFaceClassName() {
        return "LilianaDefiantNecromancer";
    }
}
