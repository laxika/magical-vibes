package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "66")
public class StepThrough extends Card {

    public StepThrough() {
        // Return two target creatures to their owners' hands.
        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Wizardcycling {2} ({2}, Discard this card: Search your library for a Wizard card,
        // reveal it, put it into your hand, then shuffle.)
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.WIZARD))),
                "Wizardcycling {2} ({2}, Discard this card: Search your library for a Wizard card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
