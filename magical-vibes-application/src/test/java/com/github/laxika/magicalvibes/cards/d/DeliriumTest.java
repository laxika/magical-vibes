package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Delirium.class, FemerefScouts.class, Forest.class})
class DeliriumTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target and it deals damage equal to its power to its controller")
    void tapsAndDealsPowerDamageToController() {
        harness.forceActivePlayer(player2);
        harness.setLife(player2, 20);
        Permanent target = addCreature(player2, 3, 3);

        castDelirium(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and dealt by the target creature")
    void preventsCombatDamageToAndByTarget() {
        harness.forceActivePlayer(player2);
        Permanent target = addCreature(player2, 3, 3);

        castDelirium(target);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(target.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(target.getId());
    }

    @Test
    @DisplayName("Prevents combat damage dealt to and by the target creature in combat")
    void preventsCombatDamageInBothDirections() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreature(player2, 2, 2);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        Permanent blocker = addCreature(player1, 3, 3);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        castDelirium(target);
        resolveCombat(player2);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target an already tapped creature")
    void canTargetAlreadyTappedCreature() {
        harness.forceActivePlayer(player2);
        harness.setLife(player2, 20);
        Permanent target = addCreature(player2, 3, 3);
        target.tap();

        castDelirium(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot be cast during the caster's own turn")
    void cannotBeCastOnOwnTurn() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreature(player2, 3, 3);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature the active player does not control")
    void cannotTargetOwnCreature() {
        harness.forceActivePlayer(player2);
        Permanent own = addCreature(player1, 2, 2);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castDelirium(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player owner, int power, int toughness) {
        Card creature = new FemerefScouts();
        creature.setPower(power);
        creature.setToughness(toughness);
        return addCreatureReady(owner, creature);
    }
}
