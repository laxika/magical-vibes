package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.c.Cancel;
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

@CardUsed({PhantasmalBear.class, Shock.class, GiantGrowth.class, ProdigalPyromancer.class,
        AetherAdept.class, Cancel.class})
class PhantasmalBearTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Bear is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent bear = addCreatureReady(player1, new PhantasmalBear());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bear.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Bear");
        harness.assertInGraveyard(player1, "Phantasmal Bear");
    }

    @Test
    @DisplayName("Phantasmal Bear is sacrificed when targeted by its controller's own spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent bear = addCreatureReady(player1, new PhantasmalBear());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Bear");
        harness.assertInGraveyard(player1, "Phantasmal Bear");
    }

    @Test
    @DisplayName("Phantasmal Bear is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent bear = addCreatureReady(player1, new PhantasmalBear());

        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, bear.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Bear");
        harness.assertInGraveyard(player1, "Phantasmal Bear");
    }

    @Test
    @DisplayName("A triggered ability targeting Phantasmal Bear sacrifices it before returning it to hand")
    void sacrificedWhenTargetedByTriggeredAbility() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new PhantasmalBear());
        harness.setHand(player1, List.of(new AetherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());

        harness.assertOnBattlefield(player1, "Phantasmal Bear");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Bear");
        harness.assertInGraveyard(player1, "Phantasmal Bear");
        resolveAllTriggers();
        harness.assertNotInHand(player1, "Phantasmal Bear");
    }

    @Test
    @DisplayName("Countering the targeting spell does not prevent Phantasmal Bear's sacrifice")
    void stillSacrificedAfterTargetingSpellIsCountered() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new PhantasmalBear());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bear.getId());
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Phantasmal Bear");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Bear");
        harness.assertInGraveyard(player1, "Phantasmal Bear");
    }

    @Test
    @DisplayName("A spell targeting its controller does not trigger Phantasmal Bear")
    void notSacrificedWhenSpellTargetsPlayer() {
        harness.addToBattlefield(player1, new PhantasmalBear());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Phantasmal Bear");
        harness.assertNotInGraveyard(player1, "Phantasmal Bear");
    }
}
