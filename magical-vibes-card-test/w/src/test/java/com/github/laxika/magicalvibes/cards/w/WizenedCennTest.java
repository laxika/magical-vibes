package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedCenn.class, KnightOfMeadowgrain.class, HillcomberGiant.class, AvianChangeling.class})
class WizenedCennTest extends BaseCardTest {
    @Test
    @DisplayName("Changeling creatures receive the Kithkin bonus only under the same controller")
    void buffsOwnChangelingOnly() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent ownChangeling = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        Permanent opponentChangeling = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());

        assertThat(gqs.getEffectivePower(gd, ownChangeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownChangeling)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentChangeling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentChangeling)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kithkin entering after Wizened Cenn receive the bonus immediately")
    void buffsKithkinResolvingAfterSource() {
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.setHand(player1, List.of(new KnightOfMeadowgrain()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kithkin = findPermanent(player1, "Knight of Meadowgrain");
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other Kithkin creatures you control get +1/+1")
    void buffsOtherKithkinYouControl() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wizened Cenn does not buff itself")
    void doesNotBuffItself() {
        Permanent cenn = harness.addToBattlefieldAndReturn(player1, new WizenedCenn());

        assertThat(gqs.getEffectivePower(gd, cenn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cenn)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Kithkin creatures")
    void doesNotBuffNonKithkin() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's Kithkin creatures")
    void doesNotBuffOpponentKithkin() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent opponentKithkin = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());

        assertThat(gqs.getEffectivePower(gd, opponentKithkin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKithkin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Wizened Cenns buff each other")
    void twoCennsBuffEachOther() {
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new WizenedCenn());

        List<Permanent> cenns = findPermanents(player1, "Wizened Cenn");

        assertThat(cenns).hasSize(2);
        for (Permanent cenn : cenns) {
            assertThat(gqs.getEffectivePower(gd, cenn)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, cenn)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Wizened Cenns give +2/+2 to other Kithkin")
    void twoCennsStackBonuses() {
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());

        // 2/2 base + 2/2 from two Cenns = 4/4
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Wizened Cenn leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Wizened Cenn"));

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Wizened Cenn resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);

        harness.setHand(player1, List.of(new WizenedCenn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(3);
    }
}
