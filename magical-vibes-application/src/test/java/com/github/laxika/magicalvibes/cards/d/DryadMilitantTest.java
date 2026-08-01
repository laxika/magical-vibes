package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DryadMilitantTest extends BaseCardTest {

    @Test
    @DisplayName("An instant resolved while Dryad Militant is on the battlefield is exiled")
    void resolvedInstantIsExiled() {
        harness.addToBattlefield(player1, new DryadMilitant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("A non-instant or non-sorcery card still goes to its graveyard")
    void nonSpellCardStillEntersGraveyard() {
        harness.addToBattlefield(player1, new DryadMilitant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An instant dealing lethal damage to Dryad Militant is exiled after it resolves")
    void lethalInstantLeavesWithDryad() {
        harness.addToBattlefield(player1, new DryadMilitant());
        UUID dryadId = harness.getPermanentId(player1, "Dryad Militant");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castInstant(player2, 0, dryadId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dryad Militant"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
