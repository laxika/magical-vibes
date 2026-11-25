package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "MH2", collectorNumber = "196")
public class FoundryHelix extends Card {

    public FoundryHelix() {
        addCastingCost(new SacrificePermanentCost(
                new PermanentTruePredicate(), "a permanent", false));
        addSpellEffect(DealDamageEffect.toAnyTarget(4));
        addSpellEffect(new ConditionalEffect(
                new SacrificedCardMatches(new CardTypePredicate(CardType.ARTIFACT), "an artifact"),
                new GainLifeEffect(4)));
    }
}
