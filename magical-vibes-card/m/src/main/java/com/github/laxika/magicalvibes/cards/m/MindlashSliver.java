package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "118")
public class MindlashSliver extends Card {

    public MindlashSliver() {
        ActivatedAbility discardAbility = new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DiscardEffect(1, Subjects.eachPlayer())),
                "{1}, Sacrifice this permanent: Each player discards a card."
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addStaticEffect(new GrantActivatedAbilityEffect(
                discardAbility,
                Subjects.otherCreatures(),
                sliver
        ));
        addStaticEffect(new GrantActivatedAbilityEffect(
                discardAbility,
                Subjects.source(),
                sliver
        ));
    }
}
