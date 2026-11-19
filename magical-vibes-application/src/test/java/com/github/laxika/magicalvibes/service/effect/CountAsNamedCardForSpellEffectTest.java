package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CountAsNamedCardForSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountAsNamedCardForSpellEffectTest extends BaseCardTest {

    private static final CardNamedPredicate COUNTED_NAME = new CardNamedPredicate("Counted Card");
    private static final CardsInGraveyard COUNT = new CardsInGraveyard(COUNTED_NAME, CountScope.ANY_PLAYER);

    private PredicateEvaluationService predicates;
    private AmountEvaluationService amounts;
    private Card graveyardCard;

    @BeforeEach
    void setUpCounter() {
        predicates = new PredicateEvaluationService(gqs);
        amounts = new AmountEvaluationService(predicates, gqs);
        graveyardCard = new Card();
        graveyardCard.setName("Actual Name");
        graveyardCard.setType(CardType.CREATURE);
        graveyardCard.addStaticEffect(new CountAsNamedCardForSpellEffect("Counting Spell", "Counted Card"));
        harness.setGraveyard(player1, List.of(graveyardCard));
    }

    @Test
    @DisplayName("Counts the card as named only for the specified spell, without changing its name")
    void appliesOnlyToTheSpecifiedSpellWithoutChangingTheCardName() {
        assertThat(amounts.evaluate(gd, COUNT, context("Counting Spell", StackEntryType.INSTANT_SPELL)))
                .isEqualTo(1);
        assertThat(amounts.evaluate(gd, COUNT, context("Other Spell", StackEntryType.INSTANT_SPELL)))
                .isZero();
        assertThat(graveyardCard.getName()).isEqualTo("Actual Name");
        assertThat(predicates.matchesCardPredicate(graveyardCard, COUNTED_NAME, null)).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Does not apply to abilities whose source has the matching name")
    @EnumSource(value = StackEntryType.class, names = {"ACTIVATED_ABILITY", "TRIGGERED_ABILITY"})
    void doesNotApplyToAbilitiesEvenWithTheMatchingSourceName(StackEntryType type) {
        assertThat(amounts.evaluate(gd, COUNT, context("Counting Spell", type))).isZero();
    }

    @Test
    @DisplayName("Does not apply when no spell is resolving")
    void doesNotApplyWithoutAResolvingSpell() {
        assertThat(amounts.evaluate(gd, COUNT, AmountContext.forCasting(player1.getId()))).isZero();
    }

    @Test
    @DisplayName("Compound filters count the card once and see no other changed characteristics")
    void supportsCompoundFiltersWithoutDoubleCountingOrChangingOtherCharacteristics() {
        var context = context("Counting Spell", StackEntryType.SORCERY_SPELL);
        var eitherName = new CardAnyOfPredicate(List.of(COUNTED_NAME, new CardNamedPredicate("Actual Name")));
        var creatureWithEitherName = new CardAllOfPredicate(List.of(eitherName, new CardTypePredicate(CardType.CREATURE)));
        var instantWithCountedName = new CardAllOfPredicate(List.of(COUNTED_NAME, new CardTypePredicate(CardType.INSTANT)));

        assertThat(amounts.evaluate(gd, new CardsInGraveyard(creatureWithEitherName, CountScope.ANY_PLAYER), context))
                .isEqualTo(1);
        assertThat(amounts.evaluate(gd, new CardsInGraveyard(instantWithCountedName, CountScope.ANY_PLAYER), context))
                .isZero();
        assertThat(amounts.evaluate(gd, new CardsInGraveyard(new CardNotPredicate(COUNTED_NAME), CountScope.ANY_PLAYER), context))
                .isZero();
    }

    @Test
    @DisplayName("Does not apply to the card in exile")
    void doesNotApplyInExile() {
        harness.setGraveyard(player1, List.of());
        gd.addToExile(player1.getId(), graveyardCard);

        assertThat(amounts.evaluate(gd, new CardsInExile(COUNTED_NAME, CountScope.ANY_PLAYER),
                context("Counting Spell", StackEntryType.INSTANT_SPELL))).isZero();
    }

    private AmountContext context(String name, StackEntryType type) {
        Card source = Card.namedRuntimePlaceholder(name);
        var entry = type == StackEntryType.TRIGGERED_ABILITY
                ? com.github.laxika.magicalvibes.testutil.TestStackDoor.triggered(source, player1.getId(), name, List.of())
                : new StackEntry(type, source, player1.getId(), name, List.of());
        return AmountContext.forStackEntry(entry, null);
    }
}
