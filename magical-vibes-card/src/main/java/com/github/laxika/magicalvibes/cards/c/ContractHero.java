package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MSC", collectorNumber = "683")
public class ContractHero extends Card {

    public ContractHero() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
                        new PermanentIsArtifactPredicate(), 0, 2, 0, "an artifact"),
                "Sacrifice an artifact or discard a card?"));
    }
}
