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
    @DisplayName("Can target a black creature you control")
    void canTargetBlackCreatureYouControl() {
        setupGriffin();
        Permanent target = addCreatureReady(player1, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        harness.assertNotOnBattlefield(player1, "Scathe Zombies");
        harness.assertInGraveyard(player1, "Scathe Zombies");
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

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
        griffin.setSummoningSick(true);
        griffin.tap();
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daraja Griffin");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("An illegal target does not sacrifice the Griffin")
    void illegalTargetDoesNotPaySacrificeCost() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Daraja Griffin");
        harness.assertNotInGraveyard(player1, "Daraja Griffin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice remains paid when the target leaves before resolution")
    void targetLeavingDoesNotRefundSacrifice() {
        setupGriffin();
        Permanent target = addCreatureReady(player2, new ScatheZombies());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        harness.assertInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.stack).isEmpty();
    }
    private void setupGriffin() {
        addCreatureReady(player1, new DarajaGriffin());
        harness.forceActivePlayer(player1);
    }
}
