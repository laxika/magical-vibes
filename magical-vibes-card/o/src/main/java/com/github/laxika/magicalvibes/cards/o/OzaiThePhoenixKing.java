package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerUnspentManaAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReplaceManaDrainWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "235")
public class OzaiThePhoenixKing extends Card {

    public OzaiThePhoenixKing() {
        addEffect(EffectSlot.STATIC, new ReplaceManaDrainWithColorlessEffect(ManaColor.RED));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerUnspentManaAtLeast(6),
                new StaticBoostEffect(0, 0, Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE), GrantScope.SELF)));
    }
}
