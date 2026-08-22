package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkulTheUnrepentant.class, GrizzlyBears.class})
class AkulTheUnrepentantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices three other creatures and puts a creature from hand onto the battlefield")
    void sacrificesThreeOtherCreaturesAndPutsCreatureFromHandOntoBattlefield() {
        Permanent source = addSourceWithOtherCreatures(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        prepareSorcerySpeed(player1);

        activateAndPayThreeOtherCreatures(source);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("May decline putting a creature from hand after paying the sacrifice cost")
    void mayDeclinePuttingCreatureFromHand() {
        Permanent source = addSourceWithOtherCreatures(3);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        prepareSorcerySpeed(player1);

        activateAndPayThreeOtherCreatures(source);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
    }

    @Test
    @DisplayName("Requires three creatures other than Akul")
    void requiresThreeOtherCreatures() {
        Permanent source = addSourceWithOtherCreatures(2);
        prepareSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, permanentIndex(source), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOnceEachTurn() {
        Permanent source = addSourceWithOtherCreatures(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        prepareSorcerySpeed(player1);

        activateAndPayThreeOtherCreatures(source);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        addOtherCreatures(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, permanentIndex(source), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        Permanent source = addSourceWithOtherCreatures(3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, permanentIndex(source), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addSourceWithOtherCreatures(int count) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AkulTheUnrepentant());
        addOtherCreatures(count);
        return source;
    }

    private void addOtherCreatures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
    }

    private void prepareSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateAndPayThreeOtherCreatures(Permanent source) {
        List<UUID> creatureIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != source)
                .map(Permanent::getId)
                .toList();

        harness.activateAbility(player1, permanentIndex(source), 0, null, null);
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice choice) {
            UUID chosenId = choice.validPermanentIds().stream()
                    .filter(creatureIds::contains)
                    .findFirst()
                    .orElseThrow();
            harness.handlePermanentChosen(player1, chosenId);
        }
    }

    private int permanentIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
