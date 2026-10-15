package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrashingWumpus.class, CloudSprite.class, GerrardsIrregulars.class, ChoMannoRevolutionary.class})
class ThrashingWumpusTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent wumpus = harness.addToBattlefieldAndReturn(player1, new ThrashingWumpus());
        Permanent irregulars = harness.addToBattlefieldAndReturn(player1, new GerrardsIrregulars());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wumpus.getMarkedDamage()).isEqualTo(1);
        assertThat(irregulars.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("can be activated repeatedly without tapping")
    void canBeActivatedRepeatedlyWithoutTapping() {
        harness.setLife(player1, 20);
        Permanent wumpus = harness.addToBattlefieldAndReturn(player1, new ThrashingWumpus());
        harness.addToBattlefield(player1, new GerrardsIrregulars());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wumpus.isTapped()).isFalse();
        assertThat(wumpus.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wumpus.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Thrashing Wumpus");
        harness.assertNotOnBattlefield(player1, "Gerrard's Irregulars");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("requires black mana")
    void requiresBlackMana() {
        harness.addToBattlefield(player1, new ThrashingWumpus());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent wumpus = harness.addToBattlefieldAndReturn(player1, new ThrashingWumpus());
        wumpus.tap();
        wumpus.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wumpus.isTapped()).isTrue();
        assertThat(wumpus.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("stacked abilities still deal damage after Wumpus dies")
    void stackedAbilitiesResolveAfterSourceDies() {
        harness.addToBattlefield(player1, new ThrashingWumpus());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player1, "Thrashing Wumpus");
        harness.assertInGraveyard(player1, "Thrashing Wumpus");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("damage prevention protects a creature without preventing damage to others")
    void respectsCreatureDamagePrevention() {
        Permanent wumpus = harness.addToBattlefieldAndReturn(player1, new ThrashingWumpus());
        Permanent choManno = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(choManno.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        assertThat(wumpus.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Cloud Sprite");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
