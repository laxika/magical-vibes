package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necrobite.class, MoorlandInquisitor.class, NettleSwine.class})
class NecrobiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Necrobite targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving grants deathtouch and a regeneration shield")
    void resolvingGrantsDeathtouchAndShield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The blocker kills a bigger attacker with deathtouch and survives via regeneration")
    void deathtouchBlockerKillsAttackerAndRegenerates() {
        Permanent bears = addCreatureReady(player1, new MoorlandInquisitor());
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new NettleSwine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player2, "Nettle Swine");
        assertThat(bears.getRegenerationShield()).isEqualTo(0);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creating a shield does not tap the creature or heal existing damage")
    void shieldCreationDoesNotRegenerateImmediately() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        creature.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("An unused regeneration shield expires at end of turn")
    void unusedShieldExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isZero();
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Necrobite does not affect a creature that left and returned before resolution")
    void returnedCreatureIsANewObject() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Necrobite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, original.getId());

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        harness.passBothPriorities();

        assertThat(returned.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(returned.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Necrobite");
        assertThat(gd.stack).isEmpty();
    }
}
