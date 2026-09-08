package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "55")
public class LurkingSkirge extends Card {

    public LurkingSkirge() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new ConditionalEffect(
                new SourceIsEnchantment(),
                new BecomeCreatureEffect(3, 2,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.IMP), Set.of(Keyword.FLYING))));
    }
}
