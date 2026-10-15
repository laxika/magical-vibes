package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireBats;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonarStrike.class, GrizzlyBears.class, VampireBats.class, WallOfTanglecord.class})
class SonarStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a tapped creature and gains 3 life with a Bat")
    void dealsDamageAndGainsLifeWithBat() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        harness.addToBattlefield(player2, new VampireBats());

        castSonarStrike(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not gain life without a Bat")
    void doesNotGainLifeWithoutBat() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);

        castSonarStrike(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void canTargetBlockingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);
        target.setBlocking(true);

        castSonarStrike(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature that is not attacking or blocking")
    void cannotTargetUntappedNonCombatCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new SonarStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking, blocking, or tapped creature");
    }

    @Test
    @DisplayName("Can target an untapped attacking creature")
    void canTargetUntappedAttackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);
        target.setAttacking(true);

        castSonarStrike(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's Bat does not grant life")
    void opponentsBatDoesNotGrantLife() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        harness.addToBattlefield(player1, new VampireBats());

        castSonarStrike(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("A Bat acquired before resolution grants life")
    void batAcquiredBeforeResolutionGrantsLife() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        castSonarStrike(target.getId());
        harness.addToBattlefield(player2, new VampireBats());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A Bat lost before resolution does not grant life")
    void batLostBeforeResolutionDoesNotGrantLife() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        Permanent bat = harness.addToBattlefieldAndReturn(player2, new VampireBats());
        castSonarStrike(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bat);
        gd.playerGraveyards.get(player2.getId()).add(bat.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("An untapped noncombat target makes the whole spell fail to resolve")
    void targetUntappedBeforeResolutionPreventsLifeGain() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        harness.addToBattlefield(player2, new VampireBats());
        castSonarStrike(target.getId());
        target.untap();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player2, "Sonar Strike");
    }

    @Test
    @DisplayName("A removed target prevents damage and life gain")
    void removedTargetPreventsLifeGain() {
        harness.setLife(player2, 15);
        Permanent target = addTappedWall(player1);
        harness.addToBattlefield(player2, new VampireBats());
        castSonarStrike(target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player2, "Sonar Strike");
    }

    @Test
    @DisplayName("A lethally damaged own Bat still grants life during resolution")
    void lethallyDamagedOwnBatStillGrantsLife() {
        harness.setLife(player2, 15);
        Permanent bat = harness.addToBattlefieldAndReturn(player2, new VampireBats());
        bat.tap();

        castSonarStrike(bat.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Vampire Bats");
        harness.assertInGraveyard(player2, "Vampire Bats");
    }

    private Permanent addTappedWall(com.github.laxika.magicalvibes.model.Player player) {
        Permanent wall = harness.addToBattlefieldAndReturn(player, new WallOfTanglecord());
        wall.tap();
        return wall;
    }

    private void castSonarStrike(UUID targetId) {
        harness.setHand(player2, List.of(new SonarStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, targetId);
    }
}
