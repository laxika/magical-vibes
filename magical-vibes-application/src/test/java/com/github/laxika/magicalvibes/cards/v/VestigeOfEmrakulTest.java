package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.cards.t.TitansPresence;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VestigeOfEmrakul.class, OranRiefInvoker.class, TitansPresence.class})
class VestigeOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Vestige of Emrakul assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VestigeOfEmrakul());
        Permanent blocker = addCreatureReady(player2, new OranRiefInvoker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damaging the player")
    void cannotAssignPlayerDamageBeforeLethalBlockerDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VestigeOfEmrakul());
        Permanent blocker = addCreatureReady(player2, new OranRiefInvoker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Trample permits assigning all damage to a blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VestigeOfEmrakul());
        Permanent blocker = addCreatureReady(player2, new OranRiefInvoker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Trample deals no player damage when a blocker survives all available damage")
    void cannotTrampleOverLargerBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VestigeOfEmrakul());
        Permanent blocker = addCreatureReady(player2, new VestigeOfEmrakul());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Devoid allows Vestige of Emrakul to pay Titan's Presence's reveal cost")
    void canRevealAsColorlessCreatureForTitansPresence() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VestigeOfEmrakul());
        VestigeOfEmrakul revealed = new VestigeOfEmrakul();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }
}
