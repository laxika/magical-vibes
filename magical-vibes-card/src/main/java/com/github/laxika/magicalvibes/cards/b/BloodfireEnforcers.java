package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "93")
public class BloodfireEnforcers extends Card {

    public BloodfireEnforcers() {
        // This creature has first strike and trample as long as an instant card and a sorcery card are in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllConditions(List.of(
                        new GraveyardCardThreshold(1, new CardTypePredicate(CardType.INSTANT)),
                        new GraveyardCardThreshold(1, new CardTypePredicate(CardType.SORCERY)))),
                new StaticBoostEffect(0, 0, Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE), GrantScope.SELF)));
    }
}
