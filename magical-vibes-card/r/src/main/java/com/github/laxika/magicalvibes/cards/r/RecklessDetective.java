package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "141")
public class RecklessDetective extends Card {

    public RecklessDetective() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
                        new PermanentIsArtifactPredicate(), 1, 2, 0, "an artifact"),
                "Sacrifice an artifact or discard a card?"));
    }
}
