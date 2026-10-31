package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSNC", collectorNumber = "19")
public class BindToSecrecy extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("OGW", "83"),
            new DraftFromSpellbookEffect.SpellbookCard("M14", "90"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "102"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "85"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "59"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "102"),
            new DraftFromSpellbookEffect.SpellbookCard("EMN", "94"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "60"),
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "62"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "79"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "223"),
            new DraftFromSpellbookEffect.SpellbookCard("M11", "112"),
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "47"),
            new DraftFromSpellbookEffect.SpellbookCard("MH2", "106"),
            new DraftFromSpellbookEffect.SpellbookCard("JUD", "54"));

    public BindToSecrecy() {
        addSpellEffect(new ChooseOneEffect(List.of(
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
        addSpellEffect(new ConditionalEffect(
                new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                new DraftCardFromSpellbookEffect(SPELLBOOK)));
    }
}
