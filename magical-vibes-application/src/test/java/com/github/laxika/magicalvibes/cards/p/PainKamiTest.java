package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RoninHoundmaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PainKami.class, RoninHoundmaster.class})
class PainKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target creature, killing a 2/2 with X=2")
    void dealsXDamageKillingSmallCreature() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ronin Houndmaster");
    }

    @Test
    @DisplayName("With X=1 the damage is not lethal to a 2/2")
    void smallXLeavesCreatureAlive() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ronin Houndmaster");
    }

    @Test
    @DisplayName("Pain Kami is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 2, target.getId());

        harness.assertNotOnBattlefield(player1, "Pain Kami");
    }

    @Test
    @DisplayName("X=0 sacrifices Pain Kami but deals no damage")
    void zeroXDealsNoDamage() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ronin Houndmaster");
        harness.assertInGraveyard(player1, "Pain Kami");
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pain Kami");
        harness.assertInGraveyard(player1, "Ronin Houndmaster");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Pain Kami can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PainKami());
        source.setTapped(true);
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, target.getId());

        harness.assertInGraveyard(player1, "Pain Kami");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pain Kami can target itself but the sacrificed target is gone at resolution")
    void canTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PainKami());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, 1, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pain Kami");
        harness.assertNotOnBattlefield(player1, "Pain Kami");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a player and does not sacrifice Pain Kami on rejection")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new PainKami());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pain Kami");
        harness.assertNotInGraveyard(player1, "Pain Kami");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot pay the red component even when X is zero")
    void requiresRedManaForZeroX() {
        harness.addToBattlefield(player1, new PainKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoninHoundmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pain Kami");
        harness.assertNotInGraveyard(player1, "Pain Kami");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
