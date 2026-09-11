package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "HOB", collectorNumber = "108")
public class RaggedShortSpear extends Card {

    public RaggedShortSpear() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new DiscardAndDrawCardEffect(1, 2), "Discard a card to draw two cards?"));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
