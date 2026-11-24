package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "122")
public class RuinInTheirWake extends Card {

    public RuinInTheirWake() {
        addSpellEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                LibrarySearchDestination.REVEAL_ONLY, null, false));
        addSpellEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNamedPredicate("Wastes")))),
                new ConditionalEffect(HappenedThisWay.searched(), new MayEffect(
                        new PutCardsOntoBattlefieldEffect(Subjects.foundThisWay()).enteringTapped(),
                        "Put the revealed card onto the battlefield tapped?"))));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.foundThisWay()));
        addSpellEffect(new ShuffleLibraryEffect(false));
    }
}
