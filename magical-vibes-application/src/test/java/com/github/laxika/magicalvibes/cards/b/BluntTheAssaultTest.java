package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BluntTheAssault.class, VulshokReplica.class, GalvanicBlast.class, Mountain.class})
class BluntTheAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blunt the Assault puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BluntTheAssault.class);
    }

    @Test
    @DisplayName("Gains 1 life for each creature on the battlefield")
    void gainsLifePerCreature() {
        // Put 2 creatures on player1's battlefield, 1 on player2's = 3 total
        addCreature(player1);
        addCreature(player1);
        addCreature(player2);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains no life when no creatures on the battlefield")
    void gainsNoLifeWhenNoCreatures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Both effects resolve together: life gain and damage prevention")
    void bothEffectsResolveTogether() {
        // 3 creatures total on the battlefield
        Permanent attacker1 = addCreature(player2);
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreature(player2);
        attacker2.setAttacking(true);
        addCreature(player1);

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        // Should gain 3 life (3 creatures on battlefield)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        // Should prevent all combat damage
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    void preventsCombatDamageToPlayersAndCreatures() {
        Permanent blocked = addCreature(player1);
        Permanent unblocked = addCreature(player1);
        Permanent blocker = addCreature(player2);
        harness.setHand(player2, List.of(new BluntTheAssault()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        declareAttackers(player1, List.of(0, 1));
        harness.castAndResolveInstant(player2, 0);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
        assertThat(blocked.getMarkedDamage()).isZero();
        assertThat(unblocked.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(blocked, unblocked);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
    }

    @Test
    void doesNotPreventNoncombatDamage() {
        harness.setHand(player1, List.of(new BluntTheAssault(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    void countsOnlyCreaturesStillOnBattlefieldAtResolution() {
        Permanent removed = addCreature(player2);
        addCreature(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new VulshokReplica()));
        harness.setHand(player1, List.of(new BluntTheAssault(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, removed.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Vulshok Replica");
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new BluntTheAssault()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new VulshokReplica());
    }
}
