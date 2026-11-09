package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "280")
public class VerdantSuccession extends Card {

    public VerdantSuccession() {
        // Whenever a green nontoken creature dies, that creature's controller may search their
        // library for a card with the same name and put it onto the battlefield, then shuffle.
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.ANY),
                new MayEffect(new SearchLibraryEffect(new Fixed(1),
                        new CardSharesNameWithTriggeringPermanentPredicate(), LibrarySearchDestination.BATTLEFIELD,
                        LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER),
                        "Search your library for a card with the same name and put it onto the battlefield?",
                        null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
