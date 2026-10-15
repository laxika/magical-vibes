package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Liliana's Indignation")
@CardUsed({LilianasIndignation.class, DrownyardExplorers.class, Forest.class})
class LilianasIndignationTest extends BaseCardTest {

    @Test
    @DisplayName("Mills X cards from its controller's library and counts creature cards")
    void millsControllerLibraryAndCountsCreatures() {
        harness.setLibrary(player1, List.of(
                new DrownyardExplorers(), new Forest(), new DrownyardExplorers(), new Forest()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isNotEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.hasType(CardType.CREATURE))
                .hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore - 4);
    }

    @Test
    @DisplayName("Does not make the target lose life when no creature card was milled")
    void noCreatureCardsCauseNoLifeLoss() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        var target = harness.addToBattlefieldAndReturn(player2, new DrownyardExplorers()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero leaves the library and life unchanged")
    void zeroXMillsNothing() {
        var creature = new DrownyardExplorers();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A short library counts only creatures milled by this spell")
    void shortLibraryDoesNotCountExistingGraveyardCreatures() {
        var creature = new DrownyardExplorers();
        var land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        harness.setGraveyard(player1, List.of(new DrownyardExplorers(), new DrownyardExplorers()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, land);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The controller can target themself")
    void canTargetController() {
        harness.setLibrary(player1, List.of(new DrownyardExplorers(), new DrownyardExplorers()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }
}
