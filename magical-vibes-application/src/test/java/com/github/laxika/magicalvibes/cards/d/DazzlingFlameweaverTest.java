package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BladeJuggler;
import com.github.laxika.magicalvibes.cards.b.BodyCount;
import com.github.laxika.magicalvibes.cards.c.CinderconeSmite;
import com.github.laxika.magicalvibes.cards.h.Hackrobat;
import com.github.laxika.magicalvibes.cards.l.LightUpTheStage;
import com.github.laxika.magicalvibes.cards.r.RafterDemon;
import com.github.laxika.magicalvibes.cards.r.RixMaadiReveler;
import com.github.laxika.magicalvibes.cards.s.SkewerTheCritics;
import com.github.laxika.magicalvibes.cards.s.SpawnOfMayhem;
import com.github.laxika.magicalvibes.cards.s.SpikewheelAcrobat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DazzlingFlameweaver.class, GrizzlyBears.class, BladeJuggler.class, BodyCount.class,
        DeadRevels.class, DrillBit.class, Hackrobat.class, LightUpTheStage.class,
        RafterDemon.class, RixMaadiReveler.class, SkewerTheCritics.class, SpawnOfMayhem.class,
        SpikewheelAcrobat.class, CinderconeSmite.class})
class DazzlingFlameweaverTest extends BaseCardTest {

    @Test
    void combatDamageConjuresOneRandomSpellbookCardIntoExileWithNextTurnPermission() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        addAttacker(new GrizzlyBears());

        runCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        var exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(exiled.getName()).isIn(Set.of(
                "Blade Juggler", "Body Count", "Dead Revels", "Drill Bit", "Hackrobat",
                "Light Up the Stage", "Rafter Demon", "Rix Maadi Reveler", "Skewer the Critics",
                "Spawn of Mayhem", "Spikewheel Acrobat"));
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(exiled.getId());
    }

    @Test
    void twoCreaturesDealingCombatDamageTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());

        runCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void menaceRejectsASingleBlocker() {
        addCreatureReady(player1, new DazzlingFlameweaver());
        addCreatureReady(player2, new DazzlingFlameweaver());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new DazzlingFlameweaver());
        Permanent first = addCreatureReady(player2, new DazzlingFlameweaver());
        Permanent second = addCreatureReady(player2, new DazzlingFlameweaver());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void friendlyNoncombatDamageDoesNotTriggerWardOrConjure() {
        Permanent flameweaver = harness.addToBattlefieldAndReturn(player1, new DazzlingFlameweaver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CinderconeSmite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, flameweaver.getId());

        assertThat(flameweaver.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flameweaversOwnCombatDamageTriggersItsAbility() {
        addAttacker(new DazzlingFlameweaver());

        runCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void eachFlameweaverTriggersForTheSameCombatDamage() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        addAttacker(new DazzlingFlameweaver());

        runCombatDamage();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .allSatisfy(card -> assertThat(gd.exilePlayPermissions)
                        .containsEntry(card.getId(), player1.getId()));
    }

    @Test
    void opponentsCombatDamageDoesNotTriggerFlameweaver() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        Permanent attacker = addCreatureReady(player2, new DazzlingFlameweaver());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    void conjuredCardPermissionExpiresAtEndOfControllersNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addAttacker(new DazzlingFlameweaver());
        harness.setLibrary(player1, List.of(new DazzlingFlameweaver(), new DazzlingFlameweaver()));
        harness.setLibrary(player2, List.of(new DazzlingFlameweaver(), new DazzlingFlameweaver()));
        runCombatDamage();
        harness.passBothPriorities();
        var exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void decliningWardCountersOpponentsSpell() {
        Permanent flameweaver = harness.addToBattlefieldAndReturn(player1, new DazzlingFlameweaver());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CinderconeSmite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, flameweaver.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Cindercone Smite");
        assertThat(flameweaver.getMarkedDamage()).isZero();
    }

    @Test
    void payingThreeLifeForWardAllowsOpponentsSpellToResolve() {
        Permanent flameweaver = harness.addToBattlefieldAndReturn(player1, new DazzlingFlameweaver());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CinderconeSmite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, flameweaver.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(flameweaver.getMarkedDamage()).isEqualTo(2);
    }

    private void addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
