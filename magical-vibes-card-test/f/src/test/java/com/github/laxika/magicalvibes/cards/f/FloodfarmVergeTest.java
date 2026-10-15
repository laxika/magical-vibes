package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodfarmVerge.class, Plains.class, Island.class})
class FloodfarmVergeTest extends BaseCardTest {

    @Test
    void addsWhiteManaWithoutRestriction() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    void blueManaAbilityRequiresPlainsOrIsland() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or an Island");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    void addsBlueManaWhenControllingPlains() {
        harness.addToBattlefield(player1, new Plains());
        addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void addsBlueManaWhenControllingIsland() {
        harness.addToBattlefield(player1, new Island());
        addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void opponentsPlainsDoesNotEnableBlueMana() {
        harness.addToBattlefield(player2, new Plains());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    private Permanent addReadyVerge(Player player) {
        Permanent verge = harness.addToBattlefieldAndReturn(player, new FloodfarmVerge());
        verge.setSummoningSick(false);
        return verge;
    }

    @Test
    void tappedPlainsStillEnablesBlueMana() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        plains.tap();
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingQualifyingLandDisablesBlueManaButStillAllowsWhite() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent verge = addReadyVerge(player1);
        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerGraveyards.get(player1.getId()).add(island.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    void newlyEnteredVergeCanProduceBlueManaImmediately() {
        harness.addToBattlefield(player1, new Island());
        Permanent verge = harness.enterBattlefieldAndReturn(player1, new FloodfarmVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherVergeDoesNotEnableBlueMana() {
        Permanent verge = addReadyVerge(player1);
        addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
