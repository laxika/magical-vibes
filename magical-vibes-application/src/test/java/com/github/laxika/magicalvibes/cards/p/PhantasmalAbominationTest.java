package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalAbomination.class, GiantGrowth.class, ProdigalPyromancer.class, Shock.class})
class PhantasmalAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new PhantasmalAbomination());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new PhantasmalAbomination());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new PhantasmalAbomination());

        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }

    @Test
    @DisplayName("Targeting queues a sacrifice trigger that resolves before the spell")
    void sacrificeResolvesBeforeTargetingSpell() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new PhantasmalAbomination());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, abomination.getId());

        harness.assertOnBattlefield(player1, "Phantasmal Abomination");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantasmal Abomination");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Targeting the controller does not trigger the creature's sacrifice ability")
    void survivesSpellTargetingItsController() {
        harness.addToBattlefield(player1, new PhantasmalAbomination());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by its controller's ability")
    void sacrificedWhenTargetedByOwnAbility() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new PhantasmalAbomination());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer),
                null, abomination.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
        harness.assertOnBattlefield(player1, "Prodigal Pyromancer");
    }
}
