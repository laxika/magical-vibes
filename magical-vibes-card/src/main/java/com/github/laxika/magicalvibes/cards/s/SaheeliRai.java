package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "186")
public class SaheeliRai extends Card {

    public SaheeliRai() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ScryEffect(1), new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "+1: Scry 1. Saheeli Rai deals 1 damage to each opponent."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(CardType.ARTIFACT), null, null, Map.of(), true, true, false, false)),
                "−2: Create a token that's a copy of target artifact or creature you control, except it's an artifact in addition to its other types. That token gains haste. Exile it at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature you control"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new SearchLibraryEffect(
                        new Fixed(3),
                        new CardTypePredicate(CardType.ARTIFACT),
                        LibrarySearchDestination.BATTLEFIELD,
                        null,
                        true
                )),
                "−7: Search your library for up to three artifact cards with different names, put them onto the battlefield, then shuffle your library."
        ));
    }
}
