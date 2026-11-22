package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "230")
@CardRegistration(set = "MB2", collectorNumber = "140")
public class ProteusStaff extends Card {

    public ProteusStaff() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new PutTargetOnBottomOfLibraryEffect(),
                        new RevealCardsFromTopUntilEffect(Subjects.controllerOf(Subjects.target()), creatureCard),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                        PutCardsIntoLibraryEffect.onBottom(Subjects.restRevealedThisWay(creatureCard))),
                "{2}{U}, {T}: Put target creature on the bottom of its owner's library. That creature's controller "
                        + "reveals cards from the top of their library until they reveal a creature card. The player "
                        + "puts that card onto the battlefield and the rest on the bottom of their library in any order. "
                        + "Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
