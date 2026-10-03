package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ONC", collectorNumber = "21")
@CardRegistration(set = "ONC", collectorNumber = "31")
public class SkyhunterStrikeForce extends Card {

    public SkyhunterStrikeForce() {
        // As long as you control your commander, other creatures you control have melee.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerControlsCommander(),
                new GrantKeywordEffect(Keyword.MELEE, GrantScope.OWN_CREATURES)));
    }
}
