package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.ShatterskullGiant;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernoTrap.class, ShatterskullGiant.class, StoneworkPuma.class, MoggFanatic.class})
class InfernoTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShatterskullGiant());
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shatterskull Giant");
        harness.assertInGraveyard(player1, "Inferno Trap");
    }

    @Test
    @DisplayName("Can be cast for {R} after two creatures dealt damage this turn")
    void castsForAlternateCostAfterTwoCreaturesDealDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShatterskullGiant());
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        addCreatureReady(player2, new StoneworkPuma());
        addCreatureReady(player2, new StoneworkPuma());
        declareAttackers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shatterskull Giant");
        harness.assertInGraveyard(player1, "Inferno Trap");
    }

    @Test
    @DisplayName("One creature dealing damage does not enable the alternate cost")
    void alternateCostRequiresTwoDistinctCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShatterskullGiant());
        addCreatureReady(player2, new ShatterskullGiant());
        declareAttackers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without creature damage")
    void alternateCostUnavailableWithoutDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShatterskullGiant());
        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage from two sacrificed creatures enables the alternate cost")
    void sacrificedCreatureDamageEnablesAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShatterskullGiant());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        harness.setHand(player1, List.of(new InfernoTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shatterskull Giant");
        harness.assertInGraveyard(player1, "Inferno Trap");
    }
}
