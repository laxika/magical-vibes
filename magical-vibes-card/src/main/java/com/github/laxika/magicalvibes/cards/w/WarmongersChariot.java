package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "226")
public class WarmongersChariot extends Card {

    public WarmongersChariot() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                        new PermanentIsHostOfSourceAuraPredicate()))));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
