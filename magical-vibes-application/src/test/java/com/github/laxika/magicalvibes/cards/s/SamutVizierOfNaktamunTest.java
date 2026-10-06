package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoppercoatVanguard;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamutVizierOfNaktamun.class, GrizzlyBears.class, CoppercoatVanguard.class})
class SamutVizierOfNaktamunTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that entered this turn dealing combat damage draws a card")
    void creatureEnteredThisTurnDrawsCard() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        var attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A creature that did not enter this turn does not trigger")
    void creatureDidNotEnterThisTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        var attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Samut can attack on her entry turn without tapping and draws for her own first-strike damage")
    void samutDrawsForHerself() {
        var samut = harness.enterBattlefieldAndReturn(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new CoppercoatVanguard()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(samut.isAttacking()).isTrue();
        assertThat(samut.isTapped()).isFalse();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Each qualifying creature creates a separate draw trigger")
    void drawsForEachCreatureRatherThanEachDamagePoint() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new CoppercoatVanguard(), new CoppercoatVanguard()));
        var first = harness.enterBattlefieldAndReturn(player1, new CoppercoatVanguard());
        var second = harness.enterBattlefieldAndReturn(player1, new CoppercoatVanguard());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        first.setAttacking(true);
        second.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 14);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's newly entered creature does not trigger Samut")
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new CoppercoatVanguard()));
        var attacker = harness.enterBattlefieldAndReturn(player2, new CoppercoatVanguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("An older creature with haste still does not qualify")
    void samutFromAnEarlierTurnDoesNotDrawForHerself() {
        addCreatureReady(player1, new SamutVizierOfNaktamun()).setAttacking(true);
        harness.setLibrary(player1, List.of(new CoppercoatVanguard()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Damage dealt only to a blocking creature does not draw a card")
    void blockedCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new CoppercoatVanguard()));
        var attacker = harness.enterBattlefieldAndReturn(player1, new CoppercoatVanguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        var blocker = addCreatureReady(player2, new CoppercoatVanguard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        blocker.addBlockingTargetId(attacker.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
