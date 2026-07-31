package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SyphonSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaweftSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Manaweft Sliver grants itself the tap-for-any-color mana ability")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        Permanent manaweft = gd.playerBattlefields.get(player1.getId()).getFirst();
        manaweft.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(manaweft.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other Slivers you control gain the mana ability")
    void grantsAbilityToOtherSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new SyphonSliver());
        Permanent syphon = gd.playerBattlefields.get(player1.getId()).get(1);
        syphon.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(syphon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted mana ability obeys summoning sickness")
    void grantedAbilityObeysSummoningSickness() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new SyphonSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Non-Sliver creatures you control do not gain the mana ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Opponent's Slivers do not gain the mana ability")
    void doesNotGrantAbilityToOpponentSlivers() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player2, new SyphonSliver());
        Permanent opponentSliver = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThat(gqs.computeStaticBonus(gd, opponentSliver).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Granted mana ability goes away when Manaweft Sliver leaves the battlefield")
    void abilityRemovedWhenManaweftLeaves() {
        harness.addToBattlefield(player1, new ManaweftSliver());
        harness.addToBattlefield(player1, new SyphonSliver());
        Permanent manaweft = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent syphon = gd.playerBattlefields.get(player1.getId()).get(1);

        gd.playerBattlefields.get(player1.getId()).remove(manaweft);

        assertThat(gqs.computeStaticBonus(gd, syphon).grantedActivatedAbilities()).isEmpty();
    }
}
