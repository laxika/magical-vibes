package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariRotwurm.class, BorosRecruit.class})
class GolgariRotwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes the target player lose 1 life")
    void sacrificesCreatureAndTargetPlayerLosesLife() {
        harness.addToBattlefield(player1, new GolgariRotwurm());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Golgari Rotwurm");
    }

    @Test
    @DisplayName("The ability may sacrifice Golgari Rotwurm itself")
    void maySacrificeItself() {
        harness.addToBattlefield(player1, new GolgariRotwurm());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Golgari Rotwurm");
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new GolgariRotwurm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("A tapped Rotwurm can activate repeatedly and sacrifice itself with another activation pending")
    void canActivateRepeatedlyWhileTapped() {
        Permanent rotwurm = harness.addToBattlefieldAndReturn(player1, new GolgariRotwurm());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        rotwurm.tap();
        rotwurm.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Golgari Rotwurm");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Green mana cannot pay the black activation cost")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new GolgariRotwurm());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        harness.assertOnBattlefield(player1, "Golgari Rotwurm");
        harness.assertNotInGraveyard(player1, "Golgari Rotwurm");
        harness.assertLife(player2, 20);
    }
}
