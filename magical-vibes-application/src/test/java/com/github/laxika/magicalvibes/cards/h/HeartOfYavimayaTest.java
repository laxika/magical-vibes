package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartOfYavimaya.class, Forest.class, GrizzlyBears.class})
class HeartOfYavimayaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen Forest and the land enters")
    void entersBySacrificingForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HeartOfYavimaya()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Heart of Yavimaya");
    }

    @Test
    @DisplayName("A tapped Forest is still a legal sacrifice")
    void tappedForestIsLegalSacrifice() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new HeartOfYavimaya()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Heart of Yavimaya");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new HeartOfYavimaya()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Heart of Yavimaya");
        harness.assertInGraveyard(player1, "Heart of Yavimaya");
    }

    @Test
    @DisplayName("With no Forest the land goes straight to the graveyard without a prompt")
    void noForestSendsLandToGraveyard() {
        harness.setHand(player1, List.of(new HeartOfYavimaya()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Heart of Yavimaya");
        harness.assertInGraveyard(player1, "Heart of Yavimaya");
    }

    @Test
    @DisplayName("Mana ability adds {G}")
    void manaAbilityAddsGreen() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HeartOfYavimaya());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability gives target creature +1/+1 until end of turn")
    void pumpAbilityBoostsTargetCreature() {
        harness.addToBattlefield(player1, new HeartOfYavimaya());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void pumpAbilityTargetsOpponentsCreatureDuringTheirTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HeartOfYavimaya());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }
}
