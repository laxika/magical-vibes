package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EzioBrashNovice;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalladOfTheBlackFlag.class, JhoirasFamiliar.class, GrizzlyBears.class, EzioBrashNovice.class})
class BalladOfTheBlackFlagTest extends BaseCardTest {

    @Test
    void chapterIMillsThreeAndMayReturnAHistoricCard() {
        JhoirasFamiliar historic = new JhoirasFamiliar();
        GrizzlyBears firstNonHistoric = new GrizzlyBears();
        GrizzlyBears secondNonHistoric = new GrizzlyBears();
        harness.setLibrary(player1, List.of(historic, firstNonHistoric, secondNonHistoric));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstNonHistoric, secondNonHistoric);
    }

    @Test
    void chapterIIMillsThreeAndMayReturnAHistoricCard() {
        harness.setLibrary(player1, List.of(
                new JhoirasFamiliar(), new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void chapterIIIMillsThreeAndMayReturnAHistoricCard() {
        harness.setLibrary(player1, List.of(
                new JhoirasFamiliar(), new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void chapterIVReducesHistoricSpellsUntilEndOfTurn() {
        addSagaWithLore(3);

        advanceToNextChapter();

        harness.assertNotOnBattlefield(player1, "Ballad of the Black Flag");
        harness.castFromHand(player1, new JhoirasFamiliar(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jhoira's Familiar");
    }

    @Test
    void chapterIVDoesNotReduceNonHistoricSpells() {
        addSagaWithLore(3);
        advanceToNextChapter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringTheBattlefieldTriggersChapterI() {
        JhoirasFamiliar historic = new JhoirasFamiliar();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(historic, first, second));

        harness.castFromHand(player1, new BalladOfTheBlackFlag(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        harness.assertOnBattlefield(player1, "Ballad of the Black Flag");
    }

    @Test
    void mayDeclineReturningAHistoricCard() {
        JhoirasFamiliar historic = new JhoirasFamiliar();
        harness.setLibrary(player1, List.of(historic, new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(historic);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChooseALaterHistoricCardButReturnsOnlyOne() {
        JhoirasFamiliar artifact = new JhoirasFamiliar();
        EzioBrashNovice legendary = new EzioBrashNovice();
        BalladOfTheBlackFlag saga = new BalladOfTheBlackFlag();
        harness.setLibrary(player1, List.of(artifact, legendary, saga));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ezio, Brash Novice");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact, saga);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayReturnANonlegendarySaga() {
        BalladOfTheBlackFlag milledSaga = new BalladOfTheBlackFlag();
        harness.setLibrary(player1, List.of(milledSaga, new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ballad of the Black Flag");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).doesNotContain(milledSaga);
    }

    @Test
    void cannotReturnAHistoricCardAlreadyInTheGraveyard() {
        JhoirasFamiliar oldCard = new JhoirasFamiliar();
        gd.playerGraveyards.get(player1.getId()).add(oldCard);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(oldCard);
        harness.assertNotInHand(player1, "Jhoira's Familiar");
    }

    @Test
    void millsAllRemainingCardsWhenLibraryHasFewerThanThree() {
        JhoirasFamiliar historic = new JhoirasFamiliar();
        harness.setLibrary(player1, List.of(historic));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Jhoira's Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ballad of the Black Flag");
    }

    @Test
    void chapterIVReducesMultipleSpellsWithoutReducingColoredMana() {
        addSagaWithLore(3);
        advanceToNextChapter();
        harness.setHand(player1, List.of(new EzioBrashNovice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castFromHand(player1, new JhoirasFamiliar(), "{2}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new BalladOfTheBlackFlag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void chapterIVDoesNotReduceOpponentsSpells() {
        addSagaWithLore(3);
        advanceToNextChapter();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JhoirasFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterIVReductionExpiresAfterTheTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(3);
        advanceToNextChapter();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BalladOfTheBlackFlag());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
