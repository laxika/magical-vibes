package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "9ED", collectorNumber = "213")
public class RogueKavu extends Card {

    public RogueKavu() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(), new BoostSelfEffect(2, 0)));
    }
}
