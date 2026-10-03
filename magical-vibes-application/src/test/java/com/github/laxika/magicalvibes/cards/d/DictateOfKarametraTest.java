package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DictateOfKarametra.class, Forest.class, SimicGrowthChamber.class})
class DictateOfKarametraTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a land for mana adds one additional mana of the type it produced")
    void addsExtraManaForController() {
        harness.addToBattlefield(player1, new DictateOfKarametra());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect is symmetric and adds mana for an opponent's land tap")
    void addsExtraManaForOpponent() {
        harness.addToBattlefield(player1, new DictateOfKarametra());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Dictate of Karametra a land produces only its normal mana")
    void noExtraManaWithoutDictate() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Dictate adds one extra mana immediately, even across controllers")
    void multipleDictatesAddIndependently() {
        harness.addToBattlefield(player1, new DictateOfKarametra());
        harness.addToBattlefield(player2, new DictateOfKarametra());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("A land producing two types grants exactly one extra mana chosen by the tapping player")
    void opponentChoosesOneOfTheProducedTypes() {
        harness.addToBattlefield(player1, new DictateOfKarametra());
        harness.addToBattlefield(player2, new SimicGrowthChamber());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void canBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new DictateOfKarametra(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dictate of Karametra");
    }
}
