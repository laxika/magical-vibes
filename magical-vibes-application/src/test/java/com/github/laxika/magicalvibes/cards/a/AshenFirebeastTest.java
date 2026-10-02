package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshenFirebeast.class, Anarchist.class, AvenFlock.class})
class AshenFirebeastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature without flying")
    void dealsDamageOnlyToCreaturesWithoutFlying() {
        Permanent firebeast = addCreatureReady(player1, new AshenFirebeast());
        Permanent groundCreature = addCreatureReady(player2, new Anarchist());
        Permanent flyingCreature = addCreatureReady(player2, new AvenFlock());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebeast.getMarkedDamage()).isEqualTo(1);
        assertThat(firebeast.isTapped()).isFalse();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent firebeast = harness.addToBattlefieldAndReturn(player1, new AshenFirebeast());
        firebeast.setSummoningSick(true);
        firebeast.tap();
        Permanent groundCreature = addCreatureReady(player2, new Anarchist());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebeast.getMarkedDamage()).isEqualTo(1);
        assertThat(firebeast.isTapped()).isTrue();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations kill ground creatures on both sides and spare flyers")
    void repeatedActivationsDealLethalDamageToBothPlayersGroundCreatures() {
        Permanent firebeast = addCreatureReady(player1, new AshenFirebeast());
        addCreatureReady(player1, new Anarchist());
        addCreatureReady(player2, new Anarchist());
        Permanent flyingCreature = addCreatureReady(player2, new AvenFlock());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firebeast.getMarkedDamage()).isEqualTo(2);
        assertThat(firebeast.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Anarchist");
        harness.assertNotOnBattlefield(player2, "Anarchist");
        harness.assertInGraveyard(player1, "Anarchist");
        harness.assertInGraveyard(player2, "Anarchist");
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Aven Flock");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
