package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "BRO", collectorNumber = "63")
public class SplittingThePowerstone extends Card {

    public SplittingThePowerstone() {
        addEffect(EffectSlot.SPELL,
                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofPowerstoneToken(new Fixed(2)));
        addEffect(EffectSlot.SPELL,
                new DrawCardIfSacrificedCardMatchesEffect(new CardSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
