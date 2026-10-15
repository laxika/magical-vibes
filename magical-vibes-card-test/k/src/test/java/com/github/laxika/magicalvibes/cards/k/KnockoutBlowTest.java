package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnockoutBlow.class, AirElemental.class, FireElemental.class})
class KnockoutBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage and gains 2 life when targeting an attacking red creature")
    void dealsDamageAndGainsLifeAgainstAttackingRedCreature() {
        Permanent target = addAttacker(new FireElemental());
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, 1);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Deals 4 damage and gains 2 life when targeting a blocking nonred creature")
    void dealsDamageAndGainsLifeAgainstBlockingNonredCreature() {
        Permanent target = addBlocker(new AirElemental());
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, 3);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Cannot pay the reduced cost when targeting a nonred creature")
    void cannotPayReducedCostForNonredCreature() {
        Permanent target = addAttacker(new AirElemental());
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("The red-creature discount also applies to a blocking creature")
    void reducedCostForRedBlocker() {
        Permanent target = addBlocker(new FireElemental());
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, 1);

        harness.assertInGraveyard(player2, "Fire Elemental");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("The discount does not remove the white mana requirement")
    void reducedCostStillRequiresWhiteMana() {
        Permanent target = addAttacker(new FireElemental());
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage or life gain when the target leaves combat before resolution")
    void noLifeGainWhenTargetLeavesCombat() {
        Permanent target = addAttacker(new FireElemental());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Knockout Blow");
    }

    @Test
    @DisplayName("No life gain when the only target leaves the battlefield before resolution")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent target = addAttacker(new FireElemental());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Knockout Blow");
    }

    private void cast(Permanent target, int whiteMana) {
        harness.setHand(player1, java.util.List.of(new KnockoutBlow()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Card card) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

    private Permanent addBlocker(Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        target.setSummoningSick(false);
        target.setBlocking(true);
        target.addBlockingTargetId(attacker.getId());
        return target;
    }
}
