package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chastise.class, GrizzlyBears.class})
class ChastiseTest extends BaseCardTest {

    private void castChastise(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Chastise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("Destroys the attacking creature and controller gains life equal to its power")
    void destroysAndGainsLife() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);

        castChastise(attacker.getId());
        harness.passBothPriorities();

        // Grizzly Bears (2/2) destroyed -> into owner's graveyard
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Caster gains life equal to power (2): 15 + 2 = 17
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Life gain accounts for power modifiers")
    void lifeGainAccountsForPowerModifiers() {
        harness.setLife(player2, 10);
        Permanent attacker = addAttacker(player1);
        attacker.setPowerModifier(3); // 2 + 3 = 5 effective power

        castChastise(attacker.getId());
        harness.passBothPriorities();

        // Effective power 5 -> caster gains 5 life (10 + 5 = 15)
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Gains life even when the attacking creature regenerates")
    void gainsLifeWhenTargetRegenerates() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);
        attacker.setRegenerationShield(1);

        castChastise(attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getRegenerationShield()).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2); // valid target elsewhere so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Chastise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);

        castChastise(attacker.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        // No life gain when the spell fizzles
        harness.assertLife(player2, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Chastise");
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);

        castChastise(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Chastise");
    }
}
