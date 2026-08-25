package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "220")
public class AkawalliTheSeethingTower extends Card {

    public AkawalliTheSeethingTower() {
        GraveyardCardThreshold descendFour = new GraveyardCardThreshold(4, new CardIsPermanentPredicate());
        GraveyardCardThreshold descendEight = new GraveyardCardThreshold(8, new CardIsPermanentPredicate());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                descendFour, new StaticBoostEffect(2, 2, Set.of(), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                descendFour, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                descendEight, new StaticBoostEffect(2, 2, Set.of(), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                descendEight, new CanBeBlockedByAtMostNCreaturesEffect(1)));
    }
}
