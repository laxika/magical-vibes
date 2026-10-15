package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MSH", collectorNumber = "140")
public class KunLunWarrior extends Card {

    public KunLunWarrior() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
                        new PermanentIsArtifactPredicate(), 1, 0, 0, "an artifact"),
                "Sacrifice an artifact or discard a card?"));
    }
}
