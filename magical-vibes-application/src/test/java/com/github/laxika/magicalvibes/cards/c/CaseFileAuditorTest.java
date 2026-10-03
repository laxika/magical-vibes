package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseFileAuditor.class, CaseOfTheShatteredPact.class, BadMoon.class, Shock.class,
        GrizzlyBears.class, Forest.class, Plains.class, Swamp.class, SavannahLions.class,
        MerfolkOfThePearlTrident.class, Gravecrawler.class, GoblinPiker.class,
        CaseOfTheUneatenFeast.class})
class CaseFileAuditorTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at six cards and may put an enchantment into hand when it enters")
    void searchesForAnEnchantmentWhenItEnters() {
        BadMoon enchantment = new BadMoon();
        Shock instant = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        Forest forest = new Forest();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(instant, creature, enchantment, forest, plains, swamp));
        harness.setHand(player1, List.of(new CaseFileAuditor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(instant, creature, forest, plains, swamp);
    }

    @Test
    @DisplayName("Looks at six cards whenever its controller solves a Case")
    void searchesForAnEnchantmentWhenACaseIsSolved() {
        harness.addToBattlefield(player1, new CaseFileAuditor());
        harness.addToBattlefield(player1, new CaseOfTheShatteredPact());
        addFiveColors();

        BadMoon enchantment = new BadMoon();
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears(), enchantment,
                new Forest(), new Plains(), new Swamp()));

        solveAtEndStep();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
    }

    @Test
    @DisplayName("Allows any mana type to cast a Case")
    void allowsAnyManaTypeForCaseSpells() {
        harness.addToBattlefield(player1, new CaseFileAuditor());

        assertThat(harness.getCastingPermissionService().canSpendAnyManaTypeToCast(
                gd, player1.getId(), new CaseOfTheShatteredPact())).isTrue();
    }

    @Test
    void canDeclineAnEnchantmentFromAShortLibrary() {
        CaseOfTheShatteredPact enchantment = new CaseOfTheShatteredPact();
        Shock instant = new Shock();
        harness.setLibrary(player1, List.of(enchantment, instant));
        castAuditor();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void onlyLooksAtTheTopSixAndLeavesUnlookedCardsAboveTheRest() {
        CaseOfTheShatteredPact chosen = new CaseOfTheShatteredPact();
        CaseOfTheUneatenFeast seventh = new CaseOfTheUneatenFeast();
        Forest forest = new Forest();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        Shock firstInstant = new Shock();
        Shock secondInstant = new Shock();
        harness.setLibrary(player1, List.of(chosen, forest, plains, swamp, firstInstant, secondInstant, seventh));
        castAuditor();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                seventh, forest, plains, swamp, firstInstant, secondInstant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void finishesWithoutAChoiceWhenNoEnchantmentIsFound() {
        Forest forest = new Forest();
        Shock instant = new Shock();
        harness.setLibrary(player1, List.of(forest, instant));
        castAuditor();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void finishesNormallyWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castAuditor();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void takesOnlyOneOfMultipleEnchantments() {
        CaseOfTheShatteredPact chosen = new CaseOfTheShatteredPact();
        CaseOfTheUneatenFeast remaining = new CaseOfTheUneatenFeast();
        harness.setLibrary(player1, List.of(chosen, remaining));
        castAuditor();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(chosen.getId(), remaining.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void doesNotTriggerWhenAnOpponentSolvesACase() {
        harness.addToBattlefield(player1, new CaseFileAuditor());
        var opponentsCase = harness.addToBattlefieldAndReturn(player2, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new Gravecrawler());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player2, new GrizzlyBears());
        CaseOfTheUneatenFeast enchantment = new CaseOfTheUneatenFeast();
        harness.setLibrary(player1, List.of(enchantment));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponentsCase.isSolved()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
    }

    @Test
    void castsAWhiteCaseWithColorlessMana() {
        harness.addToBattlefield(player1, new CaseFileAuditor());
        harness.setHand(player1, List.of(new CaseOfTheUneatenFeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CaseOfTheUneatenFeast);
    }

    @Test
    void doesNotAllowColorlessManaForNonCaseSpells() {
        harness.addToBattlefield(player1, new CaseFileAuditor());
        harness.setHand(player1, List.of(new CaseFileAuditor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castCreature(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotGiveAnOpponentPermissionToSpendColorlessManaForCases() {
        harness.addToBattlefield(player2, new CaseFileAuditor());
        harness.setHand(player1, List.of(new CaseOfTheUneatenFeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castAuditor() {
        harness.setHand(player1, List.of(new CaseFileAuditor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addFiveColors() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new GrizzlyBears());
    }

    private void solveAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
