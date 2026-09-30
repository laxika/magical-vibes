package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "30")
public class PhyrexianScrapyard extends Card {

    public PhyrexianScrapyard() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}, {T}, Discard a card: Conjure a card named Phyrexian Scrapyard into your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ConjureCardNamedIntoHandEffect("Phyrexian Scrapyard", false)
                ),
                "{1}, {T}, Discard a card: Conjure a card named Phyrexian Scrapyard into your hand."
        ));

        // {2}, {T}, Sacrifice three lands named Phyrexian Scrapyard: Conjure a card named Soul of New
        // Phyrexia onto the battlefield. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeMultiplePermanentsCost(
                                3, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentNamedPredicate("Phyrexian Scrapyard")
                                ))),
                        new ConjureCardToBattlefieldEffect("Soul of New Phyrexia")
                ),
                "{2}, {T}, Sacrifice three lands named Phyrexian Scrapyard: Conjure a card named Soul of New "
                        + "Phyrexia onto the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
