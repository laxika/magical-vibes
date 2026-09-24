package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringerOfTheBlueDawn.class, DrossCrocodile.class})
class BringerOfTheBlueDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for the five-color alternate cost")
    void castsForAlternateCost() {
        harness.setHand(player1, List.of(new BringerOfTheBlueDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bringer of the Blue Dawn");
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BringerOfTheBlueDawn());
        Permanent blocker = addCreatureReady(player2, new DrossCrocodile());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Controller may draw two cards at the beginning of their upkeep")
    void drawsTwoCardsAtControllerUpkeepWhenAccepted() {
        harness.addToBattlefield(player1, new BringerOfTheBlueDawn());
        GameData gd = harness.getGameData();
        int before = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 2);
    }

    @Test
    @DisplayName("Declining the upkeep ability does not draw cards")
    void doesNotDrawWhenDeclined() {
        harness.addToBattlefield(player1, new BringerOfTheBlueDawn());
        GameData gd = harness.getGameData();
        int before = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new BringerOfTheBlueDawn());
        GameData gd = harness.getGameData();
        int before = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }
}
