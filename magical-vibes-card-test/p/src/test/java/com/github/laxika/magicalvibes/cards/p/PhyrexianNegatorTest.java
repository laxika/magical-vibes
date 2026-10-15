package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodshotCyclops;
import com.github.laxika.magicalvibes.cards.b.BloodArtist;
import com.github.laxika.magicalvibes.cards.f.FodderCannon;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianNegator.class, BloodshotCyclops.class, GoliathBeetle.class, PhyrexianMonitor.class,
        FodderCannon.class, BloodArtist.class})
class PhyrexianNegatorTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes the Negator controller sacrifice that many permanents")
    void nonCombatDamageMakesControllerSacrificeThatManyPermanents() {
        Permanent cyclops = addCreatureReady(player1, new BloodshotCyclops());
        Permanent sacrificedCreature = addCreatureReady(player1, new GoliathBeetle());
        Permanent negator = addCreatureReady(player2, new PhyrexianNegator());
        Permanent monitor1 = addCreatureReady(player2, new PhyrexianMonitor());
        Permanent monitor2 = addCreatureReady(player2, new PhyrexianMonitor());
        Permanent fodderCannon = harness.addToBattlefieldAndReturn(player2, new FodderCannon());
        Permanent monitor3 = addCreatureReady(player2, new PhyrexianMonitor());

        harness.activateAbility(player1, 0, null, negator.getId());
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player2, List.of(
                monitor1.getId(), monitor2.getId(), fodderCannon.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cyclops);
        harness.assertOnBattlefield(player2, "Phyrexian Negator");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(monitor3);
        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertInGraveyard(player2, "Phyrexian Monitor");
        harness.assertInGraveyard(player2, "Fodder Cannon");
    }

    @Test
    @DisplayName("Combat damage also makes the Negator controller sacrifice permanents")
    void combatDamageMakesControllerSacrificeThatManyPermanents() {
        Permanent attacker = addCreatureReady(player1, new PhyrexianMonitor());
        Permanent negator = addCreatureReady(player2, new PhyrexianNegator());
        harness.addToBattlefield(player2, new PhyrexianMonitor());
        harness.addToBattlefield(player2, new PhyrexianMonitor());
        harness.addToBattlefield(player2, new PhyrexianMonitor());

        attacker.setAttacking(true);

        negator.setBlocking(true);
        negator.addBlockingTarget(0);

        resolveCombat(player1);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, gd.playerBattlefields.get(player2.getId()).subList(1, 3).stream()
                .map(Permanent::getId)
                .toList());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Phyrexian Negator");
        harness.assertInGraveyard(player1, "Phyrexian Monitor");
        harness.assertInGraveyard(player2, "Phyrexian Monitor");
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent negator = addCreatureReady(player1, new PhyrexianNegator());
        Permanent sacrificedPermanent1 = addCreatureReady(player1, new PhyrexianMonitor());
        Permanent sacrificedPermanent2 = addCreatureReady(player1, new PhyrexianMonitor());
        Permanent blocker = addCreatureReady(player2, new PhyrexianMonitor());

        negator.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(
                sacrificedPermanent1.getId(), sacrificedPermanent2.getId()));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Phyrexian Monitor");
        harness.assertOnBattlefield(player1, "Phyrexian Negator");
        harness.assertInGraveyard(player1, "Phyrexian Monitor");
    }

    @Test
    @DisplayName("The trigger can sacrifice the Negator when it is the only permanent")
    void canSacrificeItselfWhenItIsTheOnlyPermanent() {
        Permanent negator = addCreatureReady(player2, new PhyrexianNegator());
        addCreatureReady(player1, new BloodshotCyclops());

        harness.activateAbility(player1, 0, null, negator.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Phyrexian Negator");
        harness.assertInGraveyard(player2, "Phyrexian Negator");
    }

    @Test
    @DisplayName("Lethal damage still requires sacrificing as many remaining permanents as possible")
    void lethalDamageTriggerSurvivesItsSource() {
        addCreatureReady(player1, new BloodshotCyclops());
        Permanent ammunition = addCreatureReady(player1, new PhyrexianNegator());
        Permanent negator = addCreatureReady(player2, new PhyrexianNegator());
        harness.addToBattlefield(player2, new PhyrexianMonitor());
        harness.addToBattlefield(player2, new FodderCannon());

        harness.activateAbility(player1, 0, null, negator.getId());
        harness.handlePermanentChosen(player1, ammunition.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Phyrexian Negator");
        harness.assertInGraveyard(player2, "Phyrexian Monitor");
        harness.assertInGraveyard(player2, "Fodder Cannon");
        harness.assertOnBattlefield(player1, "Bloodshot Cyclops");
    }

    @Test
    @DisplayName("Automatically sacrificing all available permanents sacrifices them simultaneously")
    void automaticSacrificesAreSimultaneous() {
        Permanent attacker = addCreatureReady(player1, new BloodshotCyclops());
        harness.addToBattlefield(player2, new BloodArtist());
        Permanent negator = addCreatureReady(player2, new PhyrexianNegator());
        harness.addToBattlefield(player2, new PhyrexianMonitor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attacker.setAttacking(true);
        negator.setBlocking(true);
        negator.addBlockingTarget(0);
        resolveCombat(player1);

        for (int i = 0; i < 10; i++) {
            resolveAllTriggers();
            if (!gd.interaction.isAwaitingInput()) {
                break;
            }
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player2, player1.getId());
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
        assertThat(gd.stack).isEmpty();
    }
}
