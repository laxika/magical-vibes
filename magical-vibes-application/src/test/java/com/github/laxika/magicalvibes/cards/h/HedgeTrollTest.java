package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedgeTroll.class, Plains.class})
class HedgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Plains")
    void getsBonusWhileControllerControlsPlains() {
        harness.addToBattlefield(player1, new HedgeTroll());
        harness.addToBattlefield(player1, new Plains());

        Permanent troll = findPermanent(player1, "Hedge Troll");

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the bonus without a Plains")
    void noBonusWithoutPlains() {
        harness.addToBattlefield(player1, new HedgeTroll());

        Permanent troll = findPermanent(player1, "Hedge Troll");

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Plains does not grant the bonus")
    void opponentPlainsDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new HedgeTroll());
        harness.addToBattlefield(player2, new Plains());

        Permanent troll = findPermanent(player1, "Hedge Troll");

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {W} grants a regeneration shield")
    void whiteActivationGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new HedgeTroll());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }
}
