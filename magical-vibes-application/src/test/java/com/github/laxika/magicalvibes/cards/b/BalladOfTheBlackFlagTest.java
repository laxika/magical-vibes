package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BalladOfTheBlackFlag.class, JhoirasFamiliar.class, GrizzlyBears.class})
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Ballad of the Black Flag"));
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

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

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BalladOfTheBlackFlag());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
