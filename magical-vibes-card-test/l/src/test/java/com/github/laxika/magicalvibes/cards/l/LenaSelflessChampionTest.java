package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LenaSelflessChampion.class, GrizzlyBears.class, SerraAngel.class})
class LenaSelflessChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token for each nontoken creature its controller controls, counting itself")
    void createsTokenPerNontokenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new LenaSelflessChampion(), "{4}{W}{W}");
        harness.passBothPriorities(); // resolve Lena
        harness.passBothPriorities(); // resolve the enter trigger

        long soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .count();
        assertThat(soldiers).isEqualTo(3); // Grizzly Bears, Serra Angel and Lena herself
    }

    @Test
    @DisplayName("Existing tokens do not increase the Soldier count")
    void tokensAreNotCounted() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new LenaSelflessChampion(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        long soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .count();
        assertThat(soldiers).isEqualTo(2); // Grizzly Bears and Lena; the new tokens don't count themselves
    }

    @Test
    @DisplayName("Sacrificing gives indestructible only to creatures with power less than its own")
    void sacrificeGrantsIndestructibleToWeakerCreatures() {
        harness.addToBattlefield(player1, new LenaSelflessChampion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // 4/4
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities(); // resolve Lena's enter trigger (no tokens matter here)

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(angel.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(opposing.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Uses its boosted power at the time the sacrifice cost is paid")
    void usesPowerAtCostPayment() {
        Permanent lena = harness.addToBattlefieldAndReturn(player1, new LenaSelflessChampion());
        lena.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // 5/5
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // 4/4
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(angel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        harness.addToBattlefield(player1, new LenaSelflessChampion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Creatures with power equal to Lena's do not gain indestructible")
    void equalPowerIsExcluded() {
        harness.addToBattlefield(player1, new LenaSelflessChampion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Lena, Selfless Champion");
        harness.assertInGraveyard(player1, "Lena, Selfless Champion");
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Negative power is preserved when comparing creatures with sacrificed Lena")
    void negativePowerIsNotClampedToZero() {
        Permanent lena = harness.addToBattlefieldAndReturn(player1, new LenaSelflessChampion());
        lena.setPowerModifier(-4);
        Permanent equalPower = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equalPower.setPowerModifier(-3);
        Permanent weaker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        weaker.setPowerModifier(-4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equalPower, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, weaker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Other creatures' power is compared when the ability resolves")
    void comparesPowerAtResolution() {
        harness.addToBattlefield(player1, new LenaSelflessChampion());
        Permanent growing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent shrinking = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        growing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        shrinking.setPowerModifier(-2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, growing, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shrinking, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Gaining power after resolution does not remove indestructible")
    void protectionPersistsAfterPowerIncrease() {
        harness.addToBattlefield(player1, new LenaSelflessChampion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent lateArrival = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, lateArrival, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Existing creature tokens are excluded from subsequent enter triggers")
    void excludesExistingTokensFromCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new LenaSelflessChampion());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier")).count()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new LenaSelflessChampion());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier")).count()).isEqualTo(4);
    }

    @Test
    @DisplayName("The enter trigger counts the battlefield at resolution after Lena is sacrificed")
    void countsCreaturesAtEnterTriggerResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new LenaSelflessChampion());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier")).count()).isEqualTo(1);
    }
}
