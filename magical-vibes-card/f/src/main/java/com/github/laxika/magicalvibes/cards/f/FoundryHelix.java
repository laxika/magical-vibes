package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "MH2", collectorNumber = "196")
public class FoundryHelix extends Card {

    public FoundryHelix() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(
                new PermanentTruePredicate(), "a permanent", false));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4, false));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SacrificedCardMatches(new CardTypePredicate(CardType.ARTIFACT), "an artifact"),
                new GainLifeEffect(4)));
    }
}
