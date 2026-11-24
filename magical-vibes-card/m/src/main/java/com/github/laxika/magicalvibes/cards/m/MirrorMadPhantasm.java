package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "68")
public class MirrorMadPhantasm extends Card {

    public MirrorMadPhantasm() {
        var named = new CardNamedPredicate("Mirror-Mad Phantasm");
        PlayerSubject thatPlayer = Subjects.playersThisWay(StepKind.SHUFFLE_INTO_LIBRARY);
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(ShuffleTargetPermanentIntoLibraryEffect.of(Subjects.source()),
                        new ConditionalEffect(new HappenedThisWay(StepKind.SHUFFLE_INTO_LIBRARY), SequenceEffect.of(
                                new RevealCardsFromTopUntilEffect(thatPlayer, named),
                                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(named))
                                        .underControlOf(thatPlayer),
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(named))))),
                "{1}{U}: Mirror-Mad Phantasm's owner shuffles it into their library. If that player does, they reveal cards from the top of their library until a card named Mirror-Mad Phantasm is revealed. The player puts that card onto the battlefield and all other cards revealed this way into their graveyard."
        ));
    }
}
