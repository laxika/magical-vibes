package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "176")
public class FormidableSpeaker extends Card {

    public FormidableSpeaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE)),
                        "a card"),
                "Discard a card to search your library for a creature card?"));

        var anotherPermanent = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, anotherPermanent)),
                "{1}, {T}: Untap another target permanent.",
                new PermanentPredicateTargetFilter(anotherPermanent, "Target must be another permanent")));
    }
}
