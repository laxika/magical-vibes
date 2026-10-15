package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

public class AvatarKyoshi extends Card {

    public AvatarKyoshi() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.TRAMPLE, Keyword.HEXPROOF), GrantScope.OWN_LANDS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new GreatestPowerAmongControlled())),
                "{T}: Add X mana of any one color, where X is the greatest power among creatures you control."
        ));
    }
}
