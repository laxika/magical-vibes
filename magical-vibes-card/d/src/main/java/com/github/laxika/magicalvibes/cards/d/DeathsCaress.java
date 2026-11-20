package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "59")
public class DeathsCaress extends Card {

    public DeathsCaress() {
        // Destroy target creature. If that creature was a Human, you gain life equal to its toughness.
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                        new GainLifeEffect(new TargetToughness())));
    }
}
