package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BogwaterLumaret;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumaretsFavor.class, GrizzlyBears.class, BogwaterLumaret.class})
class LumaretsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Without gaining life, no copy is made and target gets +2/+4")
    void noLifeGainedNoCopy() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        // Copy trigger always goes on the stack; the "if you gained life" clause fails at resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("A copy of Lumaret's Favor"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("After gaining life, the spell is copied and both instances resolve for +4/+8")
    void lifeGainedCopiesSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 2);

        harness.castInstant(player1, 0, bear.getId());

        GameData gd = harness.getGameData();
        // Copy trigger sits above the spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the copy trigger — it creates a copy and offers new targets.
        harness.passBothPriorities();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("A copy of Lumaret's Favor"));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Decline to choose new targets — the copy keeps the original target.
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the copy, then the original spell.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanTargetOpponentsCreatureIndependently() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        assertThat(other.getPowerModifier()).isEqualTo(2);
        assertThat(other.getToughnessModifier()).isEqualTo(4);
        assertThat(original.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(original.getPowerModifier()).isEqualTo(2);
        assertThat(original.getToughnessModifier()).isEqualTo(4);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void lifeGainedAfterCastingEnablesCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.enterBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(8);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void opponentsLifeGainDoesNotEnableCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.getGameData().lifeGainedThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void copyCanResolveWhenOriginalTargetLeavesBattlefield() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, original.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(original);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(2);
        assertThat(other.getToughnessModifier()).isEqualTo(4);
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Lumaret's Favor");
    }

    @Test
    void bothBoostsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(8);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
