package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GangrenousZombies.class, BalduvianBears.class, Swamp.class, SnowCoveredSwamp.class,
        SnowCoveredIsland.class})
class GangrenousZombiesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice deals 1 damage to each creature and each player without a snow Swamp")
    void dealsOneWithoutSnowSwamp() {
        addCreatureReady(player1, new GangrenousZombies());
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ownBear = findPermanent(player1, "Balduvian Bears");
        Permanent oppBear = findPermanent(player2, "Balduvian Bears");
        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(oppBear.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Gangrenous Zombies");
        harness.assertInGraveyard(player1, "Gangrenous Zombies");
    }

    @Test
    @DisplayName("Sacrifice deals 2 damage to each creature and each player with a snow Swamp")
    void dealsTwoWithSnowSwamp() {
        addCreatureReady(player1, new GangrenousZombies());
        addSnowSwamp(player1);
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Gangrenous Zombies");
    }

    @Test
    @DisplayName("Opponent snow Swamp does not upgrade damage")
    void ignoresOpponentSnowSwamp() {
        addCreatureReady(player1, new GangrenousZombies());
        addSnowSwamp(player2);
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent oppBear = findPermanent(player2, "Balduvian Bears");
        assertThat(oppBear.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Checks for a snow Swamp when the ability resolves")
    void checksSnowSwampAtResolution() {
        addCreatureReady(player1, new GangrenousZombies());
        Permanent snowSwamp = addSnowSwamp(player1);
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(snowSwamp);
        harness.passBothPriorities();

        Permanent oppBear = findPermanent(player2, "Balduvian Bears");
        assertThat(oppBear.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activated ability requires tap — cannot activate when tapped")
    void activatedAbilityRequiresTap() {
        Permanent zombies = addCreatureReady(player1, new GangrenousZombies());
        zombies.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability cannot be activated while the creature has summoning sickness")
    void activatedAbilityRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new GangrenousZombies());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before damage resolves")
    void sacrificesAsActivationCost() {
        addCreatureReady(player1, new GangrenousZombies());
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Gangrenous Zombies");
        harness.assertInGraveyard(player1, "Gangrenous Zombies");
        assertThat(findPermanent(player2, "Balduvian Bears").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Balduvian Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A snow Swamp gained before resolution upgrades damage")
    void snowSwampEnteringBeforeResolutionUpgradesDamage() {
        addCreatureReady(player1, new GangrenousZombies());
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);
        addSnowSwamp(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A snow Island and a nonsnow Swamp do not count as a snow Swamp")
    void snowAndSwampMustBeOnTheSamePermanent() {
        addCreatureReady(player1, new GangrenousZombies());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Balduvian Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Multiple snow Swamps still deal only 2 damage")
    void multipleSnowSwampsDoNotIncreaseDamageFurther() {
        addCreatureReady(player1, new GangrenousZombies());
        addSnowSwamp(player1);
        addSnowSwamp(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Snow-Covered Swamp");
    }

    private Permanent addSnowSwamp(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredSwamp());
    }
}
