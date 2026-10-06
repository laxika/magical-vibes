package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllvoiGaleblade.class, Forest.class})
class IllvoiGalebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} sacrifices Illvoi Galeblade and draws a card")
    void payingTwoSacrificesAndDraws() {
        harness.addToBattlefield(player1, new IllvoiGaleblade());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Illvoi Galeblade");
        harness.assertInGraveyard(player1, "Illvoi Galeblade");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Illvoi Galeblade cannot be activated without two mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new IllvoiGaleblade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Illvoi Galeblade");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new IllvoiGaleblade()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.assertNotOnBattlefield(player2, "Illvoi Galeblade");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Illvoi Galeblade");
    }

    @Test
    @DisplayName("A tapped newly entered creature can pay colored mana to draw for its controller")
    void tappedCreatureCanActivateWithColoredMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IllvoiGaleblade());
        creature.tap();
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, null);

        harness.assertInGraveyard(player2, "Illvoi Galeblade");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("One mana cannot pay the ability cost and does not sacrifice the creature")
    void oneManaCannotActivate() {
        harness.addToBattlefield(player1, new IllvoiGaleblade());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Illvoi Galeblade");
        harness.assertNotInGraveyard(player1, "Illvoi Galeblade");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
