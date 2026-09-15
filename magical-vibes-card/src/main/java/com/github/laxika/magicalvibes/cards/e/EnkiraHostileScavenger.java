package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLX", collectorNumber = "20")
public class EnkiraHostileScavenger extends Card {

    public EnkiraHostileScavenger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Walker", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new MustBeBlockedIfAbleEffect()));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackingCreaturesOfSubtype(2, CardSubtype.ZOMBIE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
