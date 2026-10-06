package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverpyreVerge.class, Island.class, Mountain.class})
class RiverpyreVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingForRedMana() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blue mana ability requires an Island or Mountain")
    void blueManaRequiresIslandOrMountain() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Mountain");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for blue mana works while controlling an Island")
    void tappingForBlueManaWithIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana works while controlling a Mountain")
    void tappingForBlueManaWithMountain() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Island does not enable blue mana")
    void opponentsIslandDoesNotEnableBlueMana() {
        harness.addToBattlefield(player2, new Island());
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Island still enables blue mana")
    void tappedIslandEnablesBlueMana() {
        harness.addToBattlefieldAndReturn(player1, new Island()).setTapped(true);
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Verge does not satisfy the Island or Mountain restriction")
    void anotherVergeDoesNotEnableBlueMana() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());
        harness.addToBattlefield(player1, new RiverpyreVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Mountain");
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The Island or Mountain restriction is checked again after the land leaves")
    void blueManaUnavailableAfterQualifyingLandLeaves() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.activateAbility(player1, 0, 1, null, null);
        verge.setTapped(false);
        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        gd.playerGraveyards.get(player1.getId()).add(mountain.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Island or a Mountain");
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Verge cannot activate either mana ability again")
    void tappedVergeCannotProduceMoreMana() {
        harness.addToBattlefieldAndReturn(player1, new RiverpyreVerge());
        harness.addToBattlefield(player1, new Island());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
