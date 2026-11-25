package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "MOR", collectorNumber = "107")
public class StompingSlabs extends Card {

    public StompingSlabs() {
        // Reveal the top seven cards; bottom them in any order. If a copy of Stomping Slabs was
        // revealed, deal 7 damage to any target (target chosen on cast — no damage if none revealed).
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(7)));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardNamedPredicate("Stomping Slabs")),
                        DealDamageEffect.toAnyTarget(7)));
    }
}
