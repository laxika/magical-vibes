package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhalfirinCommander.class, Squire.class})
class ZhalfirinCommanderTest extends BaseCardTest {

    private void addCommander() {
        addCreatureReady(player1, new ZhalfirinCommander());
        harness.addMana(player1, ManaColor.WHITE, 3);
    }

    @Test
    @DisplayName("Ability gives target Knight +1/+1 until end of turn")
    void boostsTargetKnight() {
        addCommander();
        Permanent knight = addCreatureReady(player1, new ZhalfirinCommander());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCommander();
        Permanent knight = addCreatureReady(player1, new ZhalfirinCommander());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot target a non-Knight creature")
    void rejectsNonKnightTarget() {
        addCommander();
        Permanent nonKnight = addCreatureReady(player1, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can target an opponent's Knight creature")
    void boostsOpponentsKnight() {
        addCommander();
        Permanent knight = addCreatureReady(player2, new ZhalfirinCommander());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Commander can boost itself")
    void boostsItselfWhileTappedAndSummoningSick() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new ZhalfirinCommander());
        commander.setSummoningSick(true);
        commander.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, commander.getId());
        harness.passBothPriorities();

        assertThat(commander.getEffectivePower()).isEqualTo(3);
        assertThat(commander.getEffectiveToughness()).isEqualTo(3);
        assertThat(commander.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations give cumulative boosts")
    void repeatedActivationsStack() {
        addCommander();
        harness.addMana(player1, ManaColor.WHITE, 3);
        Permanent commander = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, commander.getId());
        harness.activateAbility(player1, 0, null, commander.getId());
        resolveAllTriggers();

        assertThat(commander.getEffectivePower()).isEqualTo(4);
        assertThat(commander.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Activation requires two white mana")
    void rejectsInsufficientWhiteMana() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(commander.getEffectivePower()).isEqualTo(2);
        assertThat(commander.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost resolves after its source leaves the battlefield")
    void boostResolvesWithoutSource() {
        addCommander();
        Permanent commander = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent knight = addCreatureReady(player1, new ZhalfirinCommander());

        harness.activateAbility(player1, 0, null, knight.getId());
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerGraveyards.get(player1.getId()).add(commander.getCard());
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking shrinks each blocker without flanking separately")
    void flankingShrinksMultipleBlockers() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new Squire());
        Permanent secondBlocker = addCreatureReady(player2, new Squire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.getEffectivePower()).isZero();
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(1);
        assertThat(secondBlocker.getEffectivePower()).isZero();
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Squire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking penalty wears off at end of turn")
    void flankingPenaltyWearsOff() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Squire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent commander = addCreatureReady(player1, new ZhalfirinCommander());
        commander.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinCommander());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }
}
