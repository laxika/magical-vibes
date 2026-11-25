package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "JUD", collectorNumber = "81")
@CardRegistration(set = "DDK", collectorNumber = "67")
public class BreakingPoint extends Card {

    public BreakingPoint() {
        addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), DamageFromSourceCost.of(6),
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true))
                .withPrompt("Have it deal 6 damage to you? If no one does, destroy all creatures.")
                .payingTogether());
    }
}
