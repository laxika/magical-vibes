package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "10")
@CardRegistration(set = "TMT", collectorNumber = "203")
public class JennikaBadAppleBigSister extends Card {

    public JennikaBadAppleBigSister() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Mutant", 2, 2, CardColor.RED, List.of(CardSubtype.MUTANT), Set.of(), Set.of()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.HAND)),
                "Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, reveal it, "
                        + "put it into your hand, then shuffle.)"
        ));
    }
}
