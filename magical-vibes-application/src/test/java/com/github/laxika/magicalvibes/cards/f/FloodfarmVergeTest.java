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
        Permanent verge = new Permanent(new FloodfarmVerge());
        verge.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(verge);
        return verge;
    }
}
