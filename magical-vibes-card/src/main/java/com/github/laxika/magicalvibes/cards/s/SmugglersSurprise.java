package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "180")
public class SmugglersSurprise extends Card {

    public SmugglersSurprise() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}", "{4}{G}", "{1}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Mill four cards. You may put up to two creature and/or land cards from among the milled cards into your hand",
                        new MillControllerAndMayReturnMilledPermanentToHandEffect(
                                4,
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND))),
                                2)),
                new ChooseOneEffect.ChooseOneOption(
                        "You may put up to two creature cards from your hand onto the battlefield",
                        new PutUpToCardsFromHandOntoBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE), "creature", 2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control with power 4 or greater gain hexproof and indestructible until end of turn",
                        new GrantKeywordEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                                GrantScope.OWN_CREATURES,
                                new PermanentPowerAtLeastPredicate(4))
                )
        )));
    }
}
