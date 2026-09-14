package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AerialCaravan;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Squallmonger.class, AerialCaravan.class, FreshVolunteers.class, CloudSprite.class})
class SquallmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each player and each creature with flying")
    void damagesPlayersAndFlyers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent squallmonger = harness.addToBattlefieldAndReturn(player1, new Squallmonger());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AerialCaravan());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AerialCaravan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(squallmonger.getMarkedDamage()).isZero();
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may activate it")
    void anyPlayerMayActivateIt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Squallmonger());
        harness.addToBattlefield(player1, new CloudSprite());
        Permanent nonFlyer = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
        harness.assertInGraveyard(player1, "Cloud Sprite");
        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can be activated repeatedly without tapping")
    void canBeActivatedRepeatedlyWithoutTapping() {
        Permanent squallmonger = harness.addToBattlefieldAndReturn(player1, new Squallmonger());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AerialCaravan());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(squallmonger.isTapped()).isFalse();
        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Requires two generic mana to activate")
    void abilityRequiresTwoGenericMana() {
        harness.addToBattlefield(player1, new Squallmonger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
