package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "27")
@CardRegistration(set = "NCC", collectorNumber = "128")
public class InTooDeep extends Card {

    public InTooDeep() {
        var creaturePlaneswalkerOrClue = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.CLUE)
        ));
        target(new PermanentPredicateTargetFilter(
                creaturePlaneswalkerOrClue,
                "Target must be a creature, planeswalker, or Clue"))
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.CLUE, GrantScope.ENCHANTED_PERMANENT, true))
                .addEffect(EffectSlot.STATIC,
                        new BecomeColorlessEffect(GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{2}",
                                        List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                                        "{2}, Sacrifice this artifact: Draw a card."
                                ),
                                GrantScope.ENCHANTED_PERMANENT));
    }
}
