package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkheartSliver.class, SinewSliver.class, SerraSphinx.class})
class DarkheartSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can sacrifice themselves to gain 3 life")
    void grantsLifeGainAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SinewSliver());
        harness.addToBattlefield(player2, new SinewSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        Permanent ownSliver = findPermanent(player1, "Sinew Sliver");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Sinew Sliver");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player2, "Sinew Sliver");
    }

    @Test
    @DisplayName("A Sliver entering after Darkheart Sliver gains the ability")
    void grantsAbilityToSliverEnteringLater() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SinewSliver());
        harness.setLife(player1, 10);

        Permanent sliver = findPermanent(player1, "Sinew Sliver");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Sinew Sliver");
    }

    @Test
    @DisplayName("Darkheart Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertInGraveyard(player1, "Darkheart Sliver");
        harness.assertNotOnBattlefield(player1, "Darkheart Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain Darkheart Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SerraSphinx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Darkheart Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DarkheartSliver());
        harness.addToBattlefield(player1, new SinewSliver());
        Permanent source = findPermanent(player1, "Darkheart Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
