package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VictualSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can sacrifice themselves to gain 4 life")
    void grantsLifeGainAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.addToBattlefield(player2, new MetallicSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        Permanent ownSliver = findPermanent(player1, "Metallic Sliver");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Metallic Sliver");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player2, "Metallic Sliver");
    }

    @Test
    @DisplayName("Victual Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Victual Sliver");
        harness.assertNotOnBattlefield(player1, "Victual Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain Victual Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Victual Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        Permanent source = findPermanent(player1, "Victual Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
