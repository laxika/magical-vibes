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
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseFileAuditor.class, CaseOfTheShatteredPact.class, BadMoon.class, Shock.class,
        GrizzlyBears.class, Forest.class, Plains.class, Swamp.class, SavannahLions.class,
        MerfolkOfThePearlTrident.class, Gravecrawler.class, GoblinPiker.class})
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
