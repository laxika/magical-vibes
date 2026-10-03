package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BighornerRancher.class})
class BighornerRancherTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds green mana equal to the greatest creature power")
    void tappingAddsManaEqualToGreatestCreaturePower() {
        Permanent rancher = harness.addToBattlefieldAndReturn(player1, new BighornerRancher());
        rancher.setSummoningSick(false);
        BighornerRancher other = new BighornerRancher();
        other.setPower(4);
        harness.addToBattlefield(player1, other);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing gains life equal to the greatest other creature toughness")
    void sacrificingGainsLifeFromOtherCreatureToughness() {
        harness.addToBattlefield(player1, new BighornerRancher());
        BighornerRancher other = new BighornerRancher();
        other.setToughness(3);
        harness.addToBattlefield(player1, other);

        harness.setLife(player1, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Bighorner Rancher");
    }

    @Test
    void manaIncludesRancherItselfAndIgnoresOpponentsCreatures() {
        Permanent rancher = harness.addToBattlefieldAndReturn(player1, new BighornerRancher());
        rancher.setSummoningSick(false);
        BighornerRancher opponent = new BighornerRancher();
        opponent.setPower(8);
        harness.addToBattlefield(player2, opponent);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(rancher.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickRancherCannotActivateTapAbility() {
        Permanent rancher = harness.addToBattlefieldAndReturn(player1, new BighornerRancher());
        rancher.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(rancher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void sacrificeWithNoOtherControlledCreaturesGainsNoLife() {
        harness.addToBattlefield(player1, new BighornerRancher());
        harness.addToBattlefield(player2, new BighornerRancher());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Bighorner Rancher");
        harness.assertInGraveyard(player1, "Bighorner Rancher");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void sacrificeUsesCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new BighornerRancher());
        harness.addToBattlefield(player1, new BighornerRancher());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent rancher = harness.addToBattlefieldAndReturn(player1, new BighornerRancher());
        rancher.setSummoningSick(true);
        rancher.setTapped(true);
        harness.addToBattlefield(player1, new BighornerRancher());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Bighorner Rancher");
    }
}
