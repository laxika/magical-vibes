package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "101")
@CardRegistration(set = "MM3", collectorNumber = "103")
@CardRegistration(set = "TSR", collectorNumber = "348")
public class MoltenRain extends Card {

    public MoltenRain() {
        // Destroy target land. If that land was nonbasic, Molten Rain deals 2 damage to the land's
        // controller.
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2)));
    }
}
