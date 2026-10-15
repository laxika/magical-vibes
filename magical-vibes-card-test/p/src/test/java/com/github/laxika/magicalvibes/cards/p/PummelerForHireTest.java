package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PummelerForHire.class, HillGiant.class, AvatarOfMight.class, Shock.class})
class PummelerForHireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to the greatest power among Giants you control")
    void gainsLifeForGreatestControlledGiantPower() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.castFromHand(player1, new PummelerForHire(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("ETB ignores non-Giants and Giants controlled by an opponent")
    void ignoresNonGiantsAndOpponents() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new HillGiant());
        harness.castFromHand(player1, new PummelerForHire(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("ETB uses the greatest Giant power at resolution, including power changes")
    void usesCurrentGreatestPowerAtResolution() {
        harness.setLife(player1, 10);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new PummelerForHire());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new PummelerForHire());
        opposingGiant.setPowerModifier(10);
        harness.castFromHand(player1, new PummelerForHire(), "{4}{G}");
        harness.passBothPriorities();

        giant.setPowerModifier(3);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB gains no life when no Giants remain at resolution")
    void gainsNoLifeWithNoRemainingGiants() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new PummelerForHire(), "{4}{G}");
        harness.passBothPriorities();

        Permanent pummeler = gd.playerBattlefields.get(player1.getId()).getFirst();
        pummeler.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pummeler for Hire");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("ETB still counts another Giant after Pummeler leaves the battlefield")
    void countsRemainingGiantAfterSourceDies() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new PummelerForHire(), "{4}{G}");
        harness.passBothPriorities();

        Permanent pummeler = gd.playerBattlefields.get(player1.getId()).getLast();
        pummeler.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pummeler for Hire");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Ward counters an opposing spell when its controller cannot pay")
    void wardCountersOpposingSpellWithoutPayment() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player1, new PummelerForHire());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, pummeler.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(pummeler.getMarkedDamage()).isZero();
    }
}
