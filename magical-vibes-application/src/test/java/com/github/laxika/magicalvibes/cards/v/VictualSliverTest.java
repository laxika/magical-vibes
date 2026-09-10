package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.cards.c.CrystallineSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictualSliver.class, CrystallineSliver.class, CravenGiant.class})
class VictualSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers can sacrifice themselves to gain 4 life")
    void grantsLifeGainAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new VictualSliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new CrystallineSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new CrystallineSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Crystalline Sliver");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingSliver), null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Crystalline Sliver");
    }

    @Test
    @DisplayName("Victual Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Victual Sliver");
        harness.assertNotOnBattlefield(player1, "Victual Sliver");
    }

    @Test
    @DisplayName("Non-Slivers do not gain Victual Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new VictualSliver());
        harness.addToBattlefield(player1, new CravenGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Victual Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VictualSliver());
        harness.addToBattlefield(player1, new CrystallineSliver());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
