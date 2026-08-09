package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HibernationSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can pay 2 life to return themselves to their owner's hand")
    void grantsSelfBounceToAllSlivers() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.addToBattlefield(player2, new MetallicSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ownSliver = findPermanent(player1, "Metallic Sliver");
        int ownSliverIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver);
        harness.activateAbility(player1, ownSliverIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Metallic Sliver");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInHand(player2, "Metallic Sliver");
    }

    @Test
    @DisplayName("Hibernation Sliver can return itself to its owner's hand")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Hibernation Sliver");
        harness.assertNotOnBattlefield(player1, "Hibernation Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability is lost when Hibernation Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        harness.addToBattlefield(player1, new HibernationSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        Permanent source = findPermanent(player1, "Hibernation Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
