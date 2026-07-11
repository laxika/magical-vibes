package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThingFromTheDeepTest extends BaseCardTest {

    private long islandsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .count();
    }

    private void attackWithThing() {
        Permanent thing = new Permanent(new ThingFromTheDeep());
        thing.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thing);
        int thingIndex = gd.playerBattlefields.get(player1.getId()).indexOf(thing);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(thingIndex));
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no Island")
    void autoSacrificesWithoutIsland() {
        attackWithThing();
        harness.passBothPriorities(); // resolve attack trigger

        // No Island to pay with, so it's sacrificed automatically with no choice.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thing from the Deep"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thing from the Deep"));
    }

    @Test
    @DisplayName("Prompts a may ability when controller has an Island")
    void promptsMayAbilityWithIsland() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly one Island sacrifices it and keeps the creature")
    void acceptWithOneIsland() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(islandsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thing from the Deep"));
    }

    @Test
    @DisplayName("Accepting with two Islands lets controller choose which one to sacrifice")
    void acceptWithTwoIslandsChoosesOne() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> islandIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .map(Permanent::getId)
                .limit(1)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, islandIds);

        assertThat(islandsControlledBy(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thing from the Deep"));
    }

    @Test
    @DisplayName("Declining sacrifices the creature and keeps the Island")
    void declineSacrificesCreature() {
        harness.addToBattlefield(player1, new Island());
        attackWithThing();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thing from the Deep"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thing from the Deep"));
        assertThat(islandsControlledBy(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Island does not satisfy the requirement")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());
        attackWithThing();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thing from the Deep"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thing from the Deep"));
        assertThat(islandsControlledBy(player2.getId())).isEqualTo(1);
    }
}
