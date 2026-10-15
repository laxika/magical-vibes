package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsCompany.class, AngelOfMercy.class, GideonOfTheTrials.class, AjaniSteadfast.class})
class GideonsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on itself when its controller gains life")
    void putsTwoCountersOnLifeGain() {
        harness.addToBattlefield(player1, new GideonsCompany());
        Permanent company = findPermanent(player1, "Gideon's Company");

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(company.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a loyalty counter on a target Gideon planeswalker")
    void putsLoyaltyCounterOnTargetGideon() {
        harness.addToBattlefield(player1, new GideonsCompany());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a non-Gideon planeswalker")
    void rejectsNonGideonPlaneswalker() {
        harness.addToBattlefield(player1, new GideonsCompany());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniSteadfast());
        ajani.setCounterCount(CounterType.LOYALTY, 4);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ajani.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }
}
