package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoggleHedgeMageTest extends BaseCardTest {

    // ===== Islands gate: may tap two target permanents =====

    @Test
    @DisplayName("With two Islands, ETB may tap two target permanents")
    void islandsGateTapsTwoPermanents() {
        addLands(player1, 2, 0);
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNoggle();
        harness.passBothPriorities(); // resolve creature spell -> first tap target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear1.getId());
        harness.handlePermanentChosen(player1, bear2.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the Islands trigger taps nothing")
    void islandsGateDeclinedTapsNothing() {
        addLands(player1, 2, 0);
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNoggle();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear1.getId());
        harness.handlePermanentChosen(player1, bear2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only one Island the tap trigger does not fire")
    void oneIslandDoesNotTrigger() {
        addLands(player1, 1, 0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        castNoggle();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Mountains gate: may deal 2 damage to target player or planeswalker =====

    @Test
    @DisplayName("With two Mountains, ETB may deal 2 damage to target player")
    void mountainsGateDealsTwoDamage() {
        addLands(player1, 0, 2);
        harness.setLife(player2, 20);
        castNoggle();
        harness.passBothPriorities(); // resolve creature spell -> damage target prompt (tap group skipped)

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the Mountains trigger deals no damage")
    void mountainsGateDeclinedDealsNoDamage() {
        addLands(player1, 0, 2);
        harness.setLife(player2, 20);
        castNoggle();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With only one Mountain the damage trigger does not fire")
    void oneMountainDoesNotTrigger() {
        addLands(player1, 0, 1);
        harness.setLife(player2, 20);
        castNoggle();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Neither gate met =====

    @Test
    @DisplayName("With no Islands or Mountains, neither ability triggers")
    void neitherGateTriggers() {
        harness.setLife(player2, 20);
        castNoggle();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Noggle Hedge-Mage"));
    }

    // ===== Both gates met: independent tap + damage both resolve =====

    @Test
    @DisplayName("With two Islands and two Mountains, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castNoggle();
        harness.passBothPriorities(); // resolve creature spell -> first tap target prompt

        harness.handlePermanentChosen(player1, bear1.getId());
        harness.handlePermanentChosen(player1, bear2.getId());
        harness.handlePermanentChosen(player1, player2.getId()); // damage target
        harness.passBothPriorities(); // resolve bundled ETB -> first may prompt (tap)
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true); // second may prompt (damage)

        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private void castNoggle() {
        harness.setHand(player1, List.of(new NoggleHedgeMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int islands, int mountains) {
        for (int i = 0; i < islands; i++) {
            harness.addToBattlefield(player, new Island());
        }
        for (int i = 0; i < mountains; i++) {
            harness.addToBattlefield(player, new Mountain());
        }
    }
}
