package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.f.FieryConclusion;
import com.github.laxika.magicalvibes.cards.p.Phytohydra;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostsOfTheInnocent.class, Char.class, FieryConclusion.class, GreaterMossdog.class, Phytohydra.class})
class GhostsOfTheInnocentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals half damage to its controller, rounded down")
    void halvesDamageToController() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals half damage to a permanent its controller controls, rounded down")
    void halvesDamageToControlledPermanent() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        Permanent greaterMossdog = addCreatureReady(player1, new GreaterMossdog());
        Permanent sacrifice = addCreatureReady(player2, new GreaterMossdog());
        harness.setHand(player2, List.of(new FieryConclusion()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player2, 0, greaterMossdog.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(greaterMossdog.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Greater Mossdog");
    }

    @Test
    @DisplayName("Deals half combat damage to its controller, rounded down")
    void halvesCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        GreaterMossdog attacker = new GreaterMossdog();
        attacker.setPower(5);
        attacker.setToughness(5);
        addCreatureReady(player2, attacker);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals half damage to any player, not just its controller")
    void halvesDamageToAnyPlayer() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        // Char deals 4 damage to its target and 2 damage to its controller: 2 + 1 = 3.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Still halves damage when damage cannot be prevented")
    void halvesDamageWhenDamageCannotBePrevented() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        gd.damageCantBePreventedThisTurn = true;

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals half damage to a permanent another player controls")
    void halvesDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        Permanent sacrifice = addCreatureReady(player1, new GreaterMossdog());
        Permanent target = addCreatureReady(player2, new GreaterMossdog());
        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Each copy halves damage separately, including damage from the same spell to its caster")
    void multipleCopiesHalveEachDamageEvent() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        harness.addToBattlefield(player2, new GhostsOfTheInnocent());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Halves combat damage dealt by Ghosts itself and by its blocker")
    void halvesCombatDamageToBothCreatures() {
        Permanent ghosts = addCreatureReady(player1, new GhostsOfTheInnocent());
        Permanent blocker = addCreatureReady(player2, new GreaterMossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(ghosts.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ghosts of the Innocent");
        harness.assertOnBattlefield(player2, "Greater Mossdog");
    }

    @Test
    @DisplayName("Stops halving damage as soon as Ghosts is sacrificed to cast the spell")
    void sacrificedGhostsDoesNotHalveDamage() {
        Permanent ghosts = harness.addToBattlefieldAndReturn(player1, new GhostsOfTheInnocent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());
        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), ghosts.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghosts of the Innocent");
        harness.assertInGraveyard(player2, "Greater Mossdog");
    }

    @Test
    @DisplayName("Phytohydra's controller chooses which damage replacement applies first")
    void promptsForCompetingDamageReplacements() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Phytohydra());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
