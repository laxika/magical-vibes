package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LenaSelflessChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token for each nontoken creature its controller controls, counting itself")
    void createsTokenPerNontokenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LenaSelflessChampion()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new LenaSelflessChampion()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
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
}
