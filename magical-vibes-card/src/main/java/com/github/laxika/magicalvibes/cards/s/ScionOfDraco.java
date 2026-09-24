package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "234")
public class ScionOfDraco extends Card {

    public ScionOfDraco() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Scaled(new BasicLandTypesAmongControlledLands(), 2)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.LIFELINK, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
