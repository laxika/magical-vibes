package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import com.github.laxika.magicalvibes.model.effect.EffectDuration;

@CardRegistration(set = "PTK", collectorNumber = "129")
public class YuanShaosInfantry extends Card {

    public YuanShaosInfantry() {
        // Whenever this creature attacks alone, this creature can't be blocked this combat.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(),
                new MakeCreatureUnblockableEffect(true, false, EffectDuration.UNTIL_END_OF_COMBAT)));
    }
}
