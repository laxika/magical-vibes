package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "173")
public class Rope extends Card {

    public Rope() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.REACH, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this Equipment: Draw a card."
        ));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
