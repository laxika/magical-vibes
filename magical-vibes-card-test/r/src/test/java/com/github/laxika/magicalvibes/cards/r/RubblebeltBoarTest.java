package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubblebeltBoar.class, VernadiShieldmate.class})
class RubblebeltBoarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Vernadi Shieldmate");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(bears.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Casting onto an empty battlefield lets the Boar target itself")
    void castWithoutTarget() {
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rubblebelt Boar");
        Permanent boar = findPermanent(player1, "Rubblebelt Boar");
        harness.handlePermanentChosen(player1, boar.getId());
        harness.passBothPriorities();

        assertThat(boar.getPowerModifier()).isEqualTo(2);
        assertThat(boar.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast still triggers the boost")
    void enteringWithoutCastingTriggersBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.enterBattlefieldAndReturn(player1, new RubblebeltBoar());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost resolves even if the Boar leaves the battlefield")
    void boostResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

}
