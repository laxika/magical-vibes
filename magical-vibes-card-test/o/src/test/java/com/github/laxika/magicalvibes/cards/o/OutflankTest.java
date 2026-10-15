package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BarteredCow;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Outflank.class, BarteredCow.class, KnightOfTheKeep.class})
class OutflankTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures its controller controls")
    void dealsDamageEqualToControlledCreatures() {
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        harness.addToBattlefield(player2, new KnightOfTheKeep());
        Permanent target = addAttacker(new BarteredCow());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent attacker = addCreatureReady(player1, new KnightOfTheKeep());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = addCreatureReady(player2, new BarteredCow());
        target.setBlocking(true);
        target.addBlockingTargetId(attacker.getId());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnightOfTheKeep());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Counts creatures at resolution rather than when cast")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        Permanent target = addAttacker(new BarteredCow());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new KnightOfTheKeep());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals no damage when its controller controls no creatures")
    void dealsZeroDamageWithNoCreatures() {
        Permanent target = addAttacker(new BarteredCow());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Bartered Cow");
        harness.assertInGraveyard(player1, "Outflank");
    }

    @Test
    @DisplayName("Can target its controller's attacking creature and counts it")
    void canTargetOwnAttacker() {
        Permanent target = addCreatureReady(player1, new BarteredCow());
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage a target that has stopped attacking before resolution")
    void targetMustRemainAttackingOrBlocking() {
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        Permanent target = addAttacker(new BarteredCow());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Outflank");
    }

    @Test
    @DisplayName("Lethal damage sends the targeted creature to the graveyard")
    void lethalDamageKillsTarget() {
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        harness.addToBattlefield(player1, new KnightOfTheKeep());
        Permanent target = addAttacker(new BarteredCow());

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Bartered Cow");
        harness.assertInGraveyard(player2, "Bartered Cow");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Outflank()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addAttacker(Card card) {
        Permanent target = addCreatureReady(player2, card);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

}
