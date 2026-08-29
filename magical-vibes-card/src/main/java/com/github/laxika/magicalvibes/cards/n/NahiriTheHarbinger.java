package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "247")
public class NahiriTheHarbinger extends Card {

    public NahiriTheHarbinger() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?")),
                "+2: You may discard a card. If you do, draw a card."
        ));

        PermanentAnyOfPredicate tappedArtifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsTappedPredicate()
                )),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                ))
        ));
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new ExileTargetPermanentEffect()),
                "\u22122: Exile target enchantment, tapped artifact, or tapped creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(),
                                tappedArtifactOrCreature
                        )),
                        "Target must be an enchantment, tapped artifact, or tapped creature"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new SearchLibraryEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE)
                        )),
                        LibrarySearchDestination.BATTLEFIELD,
                        true,
                        false,
                        true
                )),
                "\u22128: Search your library for an artifact or creature card, put it onto the battlefield, "
                        + "then shuffle. It gains haste. Return it to your hand at the beginning of the next end step."
        ));
    }
}
