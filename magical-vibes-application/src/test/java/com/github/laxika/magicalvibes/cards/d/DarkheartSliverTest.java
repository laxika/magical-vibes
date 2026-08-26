package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkheartSliver.class, MetallicSliver.class, GrizzlyBears.class})
class DarkheartSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can sacrifice themselves to gain 3 life")
    void grantsLifeGainAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.addToBattlefield(player2, new MetallicSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        Permanent ownSliver = findPermanent(player1, "Metallic Sliver");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Metallic Sliver");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertInGraveyard(player2, "Metallic Sliver");
    }

    @Test
    @DisplayName("Darkheart Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Darkheart Sliver");
        harness.assertNotOnBattlefield(player1, "Darkheart Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain Darkheart Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Darkheart Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        Permanent source = findPermanent(player1, "Darkheart Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
