package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteerClear.class, GiantSpider.class, GrizzlyBears.class, TrainedArynx.class})
class SteerClearTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without a Mount")
    void dealsTwoDamageWithoutMount() {
        Permanent target = addAttackingSpider();
        prepareSteerClear();
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(target.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 4 damage when a Mount was controlled as it was cast")
    void dealsFourDamageWhenMountWasControlledAsCast() {
        Permanent target = addAttackingSpider();
        GrizzlyBears mount = new GrizzlyBears();
        mount.setSubtypes(List.of(CardSubtype.MOUNT));
        harness.addToBattlefield(player2, mount);

        castSteerClear(target);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        prepareSteerClear();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Deals 2 damage to a blocking creature without a Mount")
    void dealsTwoDamageToBlockingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        target.setBlocking(true);
        prepareSteerClear();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An unsaddled Mount upgrades damage to a blocking creature")
    void unsaddledMountUpgradesDamageToBlockingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        target.setBlocking(true);
        harness.addToBattlefield(player2, new TrainedArynx());
        prepareSteerClear();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A Mount entering after casting does not upgrade damage")
    void mountEnteringAfterCastingDoesNotUpgradeDamage() {
        Permanent target = addAttackingSpider();
        castSteerClear(target);
        harness.addToBattlefield(player2, new TrainedArynx());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Mount does not upgrade damage")
    void opponentsMountDoesNotUpgradeDamage() {
        Permanent target = addAttackingSpider();
        harness.addToBattlefield(player1, new TrainedArynx());
        prepareSteerClear();

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature removed from combat is an illegal target on resolution")
    void creatureRemovedFromCombatReceivesNoDamage() {
        Permanent target = addAttackingSpider();
        castSteerClear(target);
        target.setAttacking(false);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Steer Clear");
    }

    @Test
    @DisplayName("The damage upgrade remains after a real Mount leaves the battlefield")
    void damageUpgradeRemainsAfterMountLeavesBattlefield() {
        Permanent target = addAttackingSpider();
        Permanent mount = harness.addToBattlefieldAndReturn(player2, new TrainedArynx());
        castSteerClear(target);
        gd.playerBattlefields.get(player2.getId()).remove(mount);
        gd.playerGraveyards.get(player2.getId()).add(mount.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    private Permanent addAttackingSpider() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        target.setSummoningSick(false);
        target.setAttacking(true);
        return target;
    }

    private void castSteerClear(Permanent target) {
        prepareSteerClear();
        harness.castInstant(player2, 0, target.getId());
    }

    private void prepareSteerClear() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new SteerClear()));
        harness.addMana(player2, ManaColor.WHITE, 1);
    }
}
