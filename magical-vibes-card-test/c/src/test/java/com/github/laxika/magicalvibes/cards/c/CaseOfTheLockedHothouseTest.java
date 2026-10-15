package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.d.Deduce;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheLockedHothouse.class, CaseOfTheUneatenFeast.class, Forest.class, NervousGardener.class, Deduce.class})
class CaseOfTheLockedHothouseTest extends BaseCardTest {

    @Test
    @DisplayName("Allows one additional land play while unsolved")
    void allowsAdditionalLandPlayWhileUnsolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step with seven lands")
    void solvesWithSevenLands() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        addSevenLands();

        resolveEndStepTriggers();

        assertThat(hothouse.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not allow creature spells from the top while unsolved")
    void doesNotAllowCreatureSpellsBeforeSolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setLibrary(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows creature and enchantment spells from the top once solved")
    void allowsCreatureAndEnchantmentSpellsOnceSolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();

        harness.setLibrary(player1, List.of(new NervousGardener(), new CaseOfTheUneatenFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Nervous Gardener");
        harness.assertOnBattlefield(player1, "Case of the Uneaten Feast");
    }

    @Test
    @DisplayName("Does not allow instant spells from the top once solved")
    void doesNotAllowInstantSpellsFromTop() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new Deduce()));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotSolveWithSixLands() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        resolveEndStepTriggers();

        assertThat(hothouse.isSolved()).isFalse();
    }

    @Test
    void doesNotSolveOnOpponentsEndStep() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(hothouse.isSolved()).isFalse();
    }

    @Test
    void rechecksLandCountWhenSolveTriggerResolves() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeLast();

        harness.passBothPriorities();

        assertThat(hothouse.isSolved()).isFalse();
    }

    @Test
    void solvedCaseAllowsTwoTopLibraryLandPlaysButNotThree() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10);
    }

    @Test
    void unsolvedCaseDoesNotAllowTopLibraryLandPlays() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void solvedCasePrivatelyShowsEvenAnUncastableTopCardWithoutPriority() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new Deduce()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Deduce"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Deduce"));
    }

    @Test
    void solvedDesignationAndPermissionsRemainAfterLosingLands() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.setLibrary(player1, List.of(new NervousGardener()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(hothouse.isSolved()).isTrue();
        harness.assertOnBattlefield(player1, "Nervous Gardener");
    }

    @Test
    void unsolvedCaseDoesNotRevealTopCard() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setLibrary(player1, List.of(new Deduce()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Deduce"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Deduce"));
    }

    @Test
    void additionalLandAllowanceIsSharedBetweenHandAndLibrary() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void creaturePermissionDoesNotOverrideTimingOrManaCost() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new NervousGardener()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addSevenLands() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });
    }
}
