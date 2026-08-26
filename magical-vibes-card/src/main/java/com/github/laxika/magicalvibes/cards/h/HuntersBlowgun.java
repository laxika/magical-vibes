package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "LCI", collectorNumber = "255")
public class HuntersBlowgun extends Card {

    public HuntersBlowgun() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.EQUIPPED_CREATURE)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new NotControllerTurn(),
                new GrantKeywordEffect(Keyword.REACH, GrantScope.EQUIPPED_CREATURE)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
