package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EntropicDecay;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "156")
public class GraySlaad extends Card {

    public GraySlaad() {
        setBackFaceCard(new EntropicDecay());
        addCastingOption(new AdventureCast("{1}{B}"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(4, new CardTypePredicate(CardType.CREATURE)),
                new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.DEATHTOUCH), GrantScope.SELF)));
    }

    @Override
    public String getBackFaceClassName() {
        return "EntropicDecay";
    }
}
