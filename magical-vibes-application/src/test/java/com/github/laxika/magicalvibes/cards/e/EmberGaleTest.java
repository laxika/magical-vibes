package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrownerInitiate;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.p.PyreCharger;
import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberGale.class, DrownerInitiate.class, GreaterAuramancy.class, PyreCharger.class,
        ZealousGuardian.class})
class EmberGaleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each white and/or blue creature the target player controls")
    void damagesWhiteAndBlueCreatures() {
        Permanent whiteAndBlue = addCreatureReady(player2, new ZealousGuardian());
        Permanent blue = addCreatureReady(player2, new DrownerInitiate());

        castEmberGale();

        assertThat(whiteAndBlue.getMarkedDamage()).isEqualTo(1);
        assertThat(blue.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage creatures that are neither white nor blue")
    void doesNotDamageOtherColors() {
        Permanent red = addCreatureReady(player2, new PyreCharger());

        castEmberGale();

        assertThat(red.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage the caster's own white creatures")
    void doesNotDamageCastersCreatures() {
        Permanent ownWhite = addCreatureReady(player1, new ZealousGuardian());

        castEmberGale();

        assertThat(ownWhite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kills a 1-toughness white creature")
    void killsSmallWhiteCreature() {
        addCreatureReady(player2, new ZealousGuardian());

        castEmberGale();

        harness.assertNotOnBattlefield(player2, "Zealous Guardian");
        harness.assertInGraveyard(player2, "Zealous Guardian");
    }

    @Test
    @DisplayName("Target player's creatures can't block this turn")
    void creaturesCantBlock() {
        Permanent whiteAndBlue = addCreatureReady(player2, new ZealousGuardian());
        Permanent red = addCreatureReady(player2, new PyreCharger());

        castEmberGale();

        assertThat(whiteAndBlue.isCantBlockThisTurn()).isTrue();
        assertThat(red.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Does not damage a matching-color noncreature permanent")
    void onlyDamagesMatchingColorCreatures() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());

        castEmberGale();

        assertThat(enchantment.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can't-block prevents declaring blockers")
    void preventsDeclaringBlockers() {
        Permanent attacker = addCreatureReady(player1, new PyreCharger());
        addCreatureReady(player2, new DrownerInitiate());

        castEmberGale();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEmberGale() {
        harness.setHand(player1, List.of(new EmberGale()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
