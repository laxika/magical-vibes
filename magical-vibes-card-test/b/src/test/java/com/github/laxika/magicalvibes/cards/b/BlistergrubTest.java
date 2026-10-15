package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blistergrub.class, MoriokReaver.class, Swamp.class})
class BlistergrubTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blistergrub puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Blistergrub()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Blistergrub");
    }

    @Test
    @DisplayName("When Blistergrub dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        resolveCombat(); // Combat damage — Blistergrub dies

        // Blistergrub should be in graveyard
        harness.assertInGraveyard(player1, "Blistergrub");

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blistergrub");
    }

    @Test
    @DisplayName("Resolving death trigger causes each opponent to lose 2 life")
    void deathTriggerCausesOpponentLifeLoss() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        resolveCombat(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Opponent loses 2 life: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller does not lose life from Blistergrub's death trigger")
    void controllerDoesNotLoseLife() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        resolveCombat(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller's life should not be affected by the trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Death trigger life loss is logged")
    void deathTriggerLifeLossIsLogged() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        resolveCombat(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses") && log.contains("2") && log.contains("life"));
    }

    @Test
    @DisplayName("Swampwalk prevents blocking when the defender controls a Swamp")
    void swampwalkPreventsBlocking() {
        addCreatureReady(player1, new Blistergrub());
        addCreatureReady(player2, new MoriokReaver());
        harness.addToBattlefield(player2, new Swamp());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackersSwampDoesNotPreventBlocking() {
        addCreatureReady(player1, new Blistergrub());
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = addCreatureReady(player2, new MoriokReaver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    /**
     * Blistergrub attacks into a real SOM 3/2 and dies from combat damage.
     */
    private void setupCombatWhereBlistergrubDies() {
        Permanent attacker = findPermanent(player1, "Blistergrub");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MoriokReaver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
    }
}
