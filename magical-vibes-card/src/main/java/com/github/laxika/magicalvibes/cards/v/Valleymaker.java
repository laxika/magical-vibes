package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "221")
public class Valleymaker extends Card {

    public Valleymaker() {
        // {T}, Sacrifice a Mountain: This creature deals 3 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                                "Sacrifice a Mountain", false),
                        new DealDamageToTargetCreatureEffect(3)),
                "{T}, Sacrifice a Mountain: Valleymaker deals 3 damage to target creature.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));

        // {T}, Sacrifice a Forest: Choose a player. That player adds {G}{G}{G}. (mana ability)
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest", false),
                        new AwardManaToChosenPlayerEffect(ManaColor.GREEN, 3)),
                "{T}, Sacrifice a Forest: Choose a player. That player adds {G}{G}{G}."));
    }
}
