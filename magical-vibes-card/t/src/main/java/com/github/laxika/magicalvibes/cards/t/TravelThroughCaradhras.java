package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "LTC", collectorNumber = "44")
@CardRegistration(set = "LTC", collectorNumber = "127")
@CardRegistration(set = "LTC", collectorNumber = "446")
public class TravelThroughCaradhras extends Card {

    public static final String REDHORN_PASS = "Redhorn Pass";
    public static final String MINES_OF_MORIA = "Mines of Moria";

    public TravelThroughCaradhras() {
        addSpellEffect(VoteEffect.startingWithYou(REDHORN_PASS, MINES_OF_MORIA));
        addSpellEffect(new RepeatEffect(new VotesFor(REDHORN_PASS),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)));
        addSpellEffect(new RepeatEffect(new VotesFor(MINES_OF_MORIA),
                ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).build()));
        addSpellEffect(new ExileSpellEffect());
    }
}
