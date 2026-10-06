package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingLight.class, GrizzlyBears.class, HillGiant.class})
class SearingLightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature with power 2 or less")
    void destroysAttackingCreatureWithPowerTwoOrLess() {
        Permanent target = addAttacker(player2, new GrizzlyBears());

        castSearingLight(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target blocking creature with power 2 or less")
    void destroysBlockingCreatureWithPowerTwoOrLess() {
        Permanent target = addBlocker(player2, new GrizzlyBears());

        castSearingLight(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SearingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot target an attacking creature with power greater than 2")
    void cannotTargetHighPowerAttackingCreature() {
        Permanent target = addAttacker(player2, new HillGiant());

        harness.setHand(player1, List.of(new SearingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        Permanent target = addAttacker(player2, new GrizzlyBears());

        castSearingLight(target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetHighPowerBlockingCreature() {
        Permanent target = addBlocker(player2, new HillGiant());
        harness.setHand(player1, List.of(new SearingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    void fizzlesIfTargetPowerIncreasesAboveTwo() {
        Permanent target = addAttacker(player2, new GrizzlyBears());
        castSearingLight(target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Searing Light");
    }

    @Test
    void canTargetCreatureWhoseCountersReducePowerToTwo() {
        Permanent target = addAttacker(player2, new HillGiant());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castSearingLight(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void fizzlesIfTargetStopsBlocking() {
        Permanent target = addBlocker(player2, new GrizzlyBears());
        castSearingLight(target.getId());

        target.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Searing Light");
    }

    @Test
    void canDestroyOwnAttackingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());

        castSearingLight(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castSearingLight(UUID targetId) {
        harness.setHand(player1, List.of(new SearingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Card card) {
        Permanent blocker = addCreatureReady(owner, card);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
