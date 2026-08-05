package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "156")
public class DomriRade extends Card {

    public DomriRade() {
        // +1: Look at the top card of your library. If it's a creature card, you may reveal it and
        // put it into your hand.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.CREATURE), false)),
                "+1: Look at the top card of your library. If it's a creature card, you may reveal it "
                        + "and put it into your hand."
        ));

        // −2: Target creature you control fights another target creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new FightTargetsEffect()),
                "−2: Target creature you control fights another target creature.",
                null, -2, null, null,
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "First target must be a creature you control"),
                        TargetFilters.creature()
                ), 2, 2
        ));

        // −7: You get an emblem with "Creatures you control have double strike, trample, hexproof,
        // and haste."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(
                                grantToCreatures(Keyword.DOUBLE_STRIKE),
                                grantToCreatures(Keyword.TRAMPLE),
                                grantToCreatures(Keyword.HEXPROOF),
                                grantToCreatures(Keyword.HASTE)),
                        "Creatures you control have double strike, trample, hexproof, and haste.")),
                "−7: You get an emblem with \"Creatures you control have double strike, trample, "
                        + "hexproof, and haste.\""
        ));
    }

    private static GrantKeywordEffect grantToCreatures(Keyword keyword) {
        return new GrantKeywordEffect(keyword, GrantScope.OWN_PERMANENTS, new PermanentIsCreaturePredicate());
    }
}
