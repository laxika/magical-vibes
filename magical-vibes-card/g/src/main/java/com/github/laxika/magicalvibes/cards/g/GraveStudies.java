package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "YSOS", collectorNumber = "20")
public class GraveStudies extends Card {

    public GraveStudies() {
        // Conjure a card named Teacher's Pest onto the battlefield, then each player sacrifices a
        // creature.
        addSpellEffect(ConjureCardsEffect.named("SOS", "238", ConjureDestination.ontoBattlefield()));
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.eachPlayer()));
    }
}
