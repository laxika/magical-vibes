package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSNC", collectorNumber = "19")
public class BindToSecrecy extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Corpse Churn", "Corpse Hauler", "Courier Bat", "Durable Coilbug", "Fear of Death",
            "Gorging Vulture", "Liliana's Elite", "Locked in the Cemetery", "Naga Oracle",
            "Necrotic Wound", "Obsessive Stitcher", "Reassembling Skeleton", "Strategic Planning",
            "Unmarked Grave", "Wonder");

    public BindToSecrecy() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target noncreature spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryNotPredicate(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                                "Target must be a noncreature spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a duplicate of target creature card in an opponent's graveyard into your hand",
                        new ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect(),
                        new GraveyardCardPredicateTargetFilter(
                                new CardTypePredicate(CardType.CREATURE),
                                GraveyardSearchScope.OPPONENT_GRAVEYARD)))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                new DraftCardFromSpellbookEffect(SPELLBOOK)));
    }
}
