package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;

@CardRegistration(set = "ORI", collectorNumber = "106")
public class LilianaHereticalHealer extends Card {

    public LilianaHereticalHealer() {
        setBackFaceCard(new LilianaDefiantNecromancer());

        // Whenever another nontoken creature you control dies, exile Liliana, Heretical Healer, then
        // return her to the battlefield transformed under her owner's control. If you do, create a
        // 2/2 black Zombie creature token.
        // The token rides the transform as an "if you do" payload: if Liliana has already left the
        // battlefield when the trigger resolves, nothing is exiled and no Zombie is created.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new ExileSelfAndReturnTransformedEffect(CreateTokenEffect.blackZombie(1)));
    }

    @Override
    public String getBackFaceClassName() {
        return "LilianaDefiantNecromancer";
    }
}
