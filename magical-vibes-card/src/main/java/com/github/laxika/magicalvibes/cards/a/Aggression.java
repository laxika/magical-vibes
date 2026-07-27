package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreatureDidntAttack;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "169")
public class Aggression extends Card {

    public Aggression() {
        // Enchant non-Wall creature
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)))),
                "Target must be a creature that isn't a Wall"))
                // Enchanted creature has first strike and trample.
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                                GrantScope.ENCHANTED_CREATURE))
                // At the beginning of the end step of enchanted creature's controller,
                // destroy that creature if it didn't attack this turn.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED,
                        new ConditionalEffect(new EnchantedCreatureDidntAttack(),
                                new DestroyEnchantedPermanentEffect()));
    }
}
