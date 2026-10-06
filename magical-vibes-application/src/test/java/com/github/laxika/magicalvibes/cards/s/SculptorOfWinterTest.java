package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SculptorOfWinter.class, Plains.class, GrizzlyBears.class, SnowCoveredForest.class})
class SculptorOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target snow land")
    void untapsTargetSnowLand() {
        addReadySculptor(player1);
        Permanent land = addSnowLand(player1);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Sculptor of Winter").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's snow land")
    void untapsOpponentSnowLand() {
        addReadySculptor(player1);
        Permanent land = addSnowLand(player2);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addReadySculptor(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadySculptor(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSnowCreature() {
        addReadySculptor(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SculptorOfWinter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetUntappedSnowLand() {
        Permanent sculptor = addReadySculptor(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());

        harness.activateAbility(player1, 0, 0, null, land.getId());

        assertThat(sculptor.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent sculptor = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        sculptor.setSummoningSick(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sculptor.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent sculptor = addReadySculptor(player1);
        sculptor.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent sculptor = addReadySculptor(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sculptor);
        gd.playerGraveyards.get(player1.getId()).add(sculptor.getCard());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void doesNotUntapLandThatStopsBeingSnowBeforeResolution() {
        addReadySculptor(player1);
        Permanent land = addSnowLand(player1);
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySculptor(Player player) {
        return addCreatureReady(player, new SculptorOfWinter());
    }

    private Permanent addSnowLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredForest());
    }
}
