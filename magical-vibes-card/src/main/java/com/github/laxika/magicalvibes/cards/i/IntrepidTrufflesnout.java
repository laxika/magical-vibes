package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GoHogWild;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "WOE", collectorNumber = "320")
public class IntrepidTrufflesnout extends Card {

    public IntrepidTrufflesnout() {
        setBackFaceCard(new GoHogWild());
        addCastingOption(new AdventureCast("{1}{G}"));
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new AttacksAlone(), CreateTokenEffect.ofFoodToken(1)));
    }

    @Override
    public String getBackFaceClassName() {
        return "GoHogWild";
    }
}
