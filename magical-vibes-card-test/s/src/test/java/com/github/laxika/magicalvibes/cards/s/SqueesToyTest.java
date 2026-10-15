package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.r.RootwaterHunter;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SqueesToy.class, CanopySpider.class, RootwaterHunter.class, WindDrake.class})
class SqueesToyTest extends BaseCardTest {

    private int indexOf(Player controller, Permanent permanent) {
        return gd.playerBattlefields.get(controller.getId()).indexOf(permanent);
    }

    private Permanent addToy() {
        return harness.addToBattlefieldAndReturn(player1, new SqueesToy());
    }

    @Test
    @DisplayName("{T} shields the target creature for 1")
    void shieldsTargetCreature() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getDamagePreventionShield()).isEqualTo(1);
        assertThat(toy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The shield prevents only 1 combat damage to the target creature")
    void shieldPreventsOneCombatDamage() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        Permanent attacker = addCreatureReady(player2, new WindDrake());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, attacker)));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, spider), 0)));
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The shield prevents the next 1 noncombat damage")
    void shieldPreventsNoncombatDamage() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        Permanent hunter = addCreatureReady(player2, new RootwaterHunter());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, hunter), null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(0);
        assertThat(spider.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent toy = addToy();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, toy), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent toy = addToy();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, toy), null, toy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("A consumed shield does not prevent a later damage event")
    void shieldOnlyPreventsFirstDamageEvent() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        Permanent firstHunter = addCreatureReady(player2, new RootwaterHunter());
        Permanent secondHunter = addCreatureReady(player2, new RootwaterHunter());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, firstHunter), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getMarkedDamage()).isZero();

        harness.activateAbility(player2, indexOf(player2, secondHunter), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Canopy Spider");
    }

    @Test
    @DisplayName("Two Toys prevent two damage across separate events, then allow further damage")
    void shieldsStackAndConsumeAcrossDamageEvents() {
        Permanent firstToy = addToy();
        Permanent secondToy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        Permanent firstHunter = addCreatureReady(player2, new RootwaterHunter());
        Permanent secondHunter = addCreatureReady(player2, new RootwaterHunter());
        Permanent thirdHunter = addCreatureReady(player2, new RootwaterHunter());

        harness.activateAbility(player1, indexOf(player1, firstToy), null, spider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondToy), null, spider.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, firstHunter), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getMarkedDamage()).isZero();
        harness.activateAbility(player2, indexOf(player2, secondHunter), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getMarkedDamage()).isZero();
        harness.activateAbility(player2, indexOf(player2, thirdHunter), null, spider.getId());
        harness.passBothPriorities();
        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Canopy Spider");
    }

    @Test
    @DisplayName("A tapped Toy cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent toy = addToy();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());

        harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, toy), null, spider.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spider.getDamagePreventionShield()).isEqualTo(1);
    }
}
