package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "267")
public class TrueFaithCenser extends Card {

    public TrueFaithCenser() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE,
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
