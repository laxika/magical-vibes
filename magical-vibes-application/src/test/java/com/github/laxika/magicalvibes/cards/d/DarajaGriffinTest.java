package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarajaGriffin.class, GrizzlyBears.class, Pestilence.class, ScatheZombies.class})
class DarajaGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing destroys target black creature")
    void sacrificingDestroysTargetBlackCreature() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Griffin is sacrificed as a cost.
        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        // Target black creature is destroyed.
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonBlackCreature() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreaturePermanent() {
        setupGriffin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pestilence());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("A regeneration shield prevents the target's destruction")
    void regenerationPreventsDestruction() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new ScatheZombies());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
        harness.assertNotInGraveyard(player2, "Scathe Zombies");
        assertThat(target.getRegenerationShield()).isZero();
    }

    private void setupGriffin() {
        addCreatureReady(player1, new DarajaGriffin());
        harness.forceActivePlayer(player1);
    }
}
