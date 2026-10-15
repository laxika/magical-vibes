package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BriarpackAlpha.class, DawntreaderElk.class})
class BriarpackAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.passPriority(gd, player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Briarpack Alpha");
    }

    @Test
    @DisplayName("Can cast during combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Briarpack Alpha");
    }

    @Test
    @DisplayName("Creature spell can be cast without selecting its ETB target")
    void castingDoesNotRequireEtbTarget() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Briarpack Alpha");
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Briarpack Alpha");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Briarpack Alpha");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and gives target creature +2/+2")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();

        Permanent elk = findPermanent(player2, "Dawntreader Elk");
        assertThat(elk.getPowerModifier()).isEqualTo(2);
        assertThat(elk.getToughnessModifier()).isEqualTo(2);
        assertThat(elk.getEffectivePower()).isEqualTo(4);
        assertThat(elk.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent elk = findPermanent(player2, "Dawntreader Elk");
        assertThat(elk.getPowerModifier()).isEqualTo(2);
        assertThat(elk.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elk.getPowerModifier()).isEqualTo(0);
        assertThat(elk.getToughnessModifier()).isEqualTo(0);
        assertThat(elk.getEffectivePower()).isEqualTo(2);
        assertThat(elk.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent elk = findPermanent(player1, "Dawntreader Elk");
        assertThat(elk.getEffectivePower()).isEqualTo(4);
        assertThat(elk.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature — ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // Resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can cast without a target when no creatures on battlefield")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Briarpack Alpha");
    }

    @Test
    @DisplayName("Must target itself when it enters an otherwise empty battlefield")
    void targetsItselfWhenOnlyCreature() {
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Briarpack Alpha");
        UUID alphaId = harness.getPermanentId(player1, "Briarpack Alpha");
        harness.handlePermanentChosen(player1, alphaId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(alphaId);
        harness.passBothPriorities();

        Permanent alpha = findPermanent(player1, "Briarpack Alpha");
        assertThat(alpha.getEffectivePower()).isEqualTo(5);
        assertThat(alpha.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose itself after entering even with another creature present")
    void canChooseItselfWithAnotherCreaturePresent() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Briarpack Alpha"));
        harness.passBothPriorities();

        Permanent alpha = findPermanent(player1, "Briarpack Alpha");
        assertThat(alpha.getEffectivePower()).isEqualTo(5);
        assertThat(alpha.getEffectiveToughness()).isEqualTo(5);
        assertThat(findPermanent(player2, "Dawntreader Elk").getPowerModifier()).isZero();
        assertThat(findPermanent(player2, "Dawntreader Elk").getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB boost resolves independently of its source")
    void boostResolvesAfterAlphaLeaves() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BriarpackAlpha()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Dawntreader Elk"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        Permanent elk = findPermanent(player2, "Dawntreader Elk");
        assertThat(elk.getEffectivePower()).isEqualTo(4);
        assertThat(elk.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
