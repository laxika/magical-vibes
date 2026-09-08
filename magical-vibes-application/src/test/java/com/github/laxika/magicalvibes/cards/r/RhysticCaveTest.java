package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RhysticCaveTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a color before the payment decisions and produces that mana when nobody pays")
    void producesChosenColorWhenNobodyPays() {
        Permanent cave = addReadyCave();

        activate(cave);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playersWhoTappedLandForManaThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("A player paying {1} prevents the chosen mana from being produced")
    void paymentPreventsMana() {
        Permanent cave = addReadyCave();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(cave);
        harness.handleListChoice(player1, "BLUE");
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playersWhoTappedLandForManaThisTurn).doesNotContain(player1.getId());
    }

    private Permanent addReadyCave() {
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return cave;
    }

    private void activate(Permanent cave) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cave), null, null);
        harness.passBothPriorities();
    }
}
