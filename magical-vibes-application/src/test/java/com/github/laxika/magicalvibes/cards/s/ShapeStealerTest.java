package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkutaBornOfAsh;
import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.cards.g.GhostLitRaider;
import com.github.laxika.magicalvibes.cards.i.InnerCalmOuterStrength;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheCrescentMoon;
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

@CardUsed({
        ShapeStealer.class,
        KamiOfTheCrescentMoon.class,
        InnerCalmOuterStrength.class,
        AkutaBornOfAsh.class,
        GhostLitRaider.class,
        GnatMiser.class
})
class ShapeStealerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature changes Shape Stealer's base power and toughness")
    void blockingCreatureChangesBasePowerAndToughness() {
        Permanent attacker = addCreatureReady(player1, new KamiOfTheCrescentMoon());
        attacker.setAttacking(true);
        Permanent shapeStealer = addCreatureReady(player2, new ShapeStealer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Becoming blocked changes Shape Stealer's base power and toughness")
    void becomingBlockedChangesBasePowerAndToughness() {
        Permanent shapeStealer = addCreatureReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        addCreatureReady(player2, new KamiOfTheCrescentMoon());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Shape Stealer's changed base power and toughness last until end of turn")
    void changedBasePowerAndToughnessLastUntilEndOfTurn() {
        Permanent shapeStealer = addCreatureReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        addCreatureReady(player2, new KamiOfTheCrescentMoon());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the combat opponent's power and toughness when the trigger resolves")
    void usesCombatOpponentsPowerAndToughnessAtResolution() {
        Permanent shapeStealer = addCreatureReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KamiOfTheCrescentMoon());
        harness.setHand(player2, List.of(new InnerCalmOuterStrength(), new InnerCalmOuterStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(2);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Triggers once for each creature that blocks Shape Stealer")
    void triggersOnceForEachBlockingCreature() {
        Permanent shapeStealer = addCreatureReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        addCreatureReady(player2, new KamiOfTheCrescentMoon());
        addCreatureReady(player2, new GnatMiser());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses the combat opponent's last known power and toughness if it leaves before resolution")
    void usesCombatOpponentsLastKnownPowerAndToughnessAfterItLeaves() {
        Permanent shapeStealer = addCreatureReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AkutaBornOfAsh());
        harness.setHand(player2, List.of(new GhostLitRaider()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.activateHandAbility(player2, 0, blocker.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Akuta, Born of Ash");
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(3);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(2);
    }
}
