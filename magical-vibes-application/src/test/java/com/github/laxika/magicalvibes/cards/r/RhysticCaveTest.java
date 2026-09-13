package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RhysticCave.class)
class RhysticCaveTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the ability on the stack before choosing a color")
    void abilityWaitsOnStackForResolution() {
        Permanent cave = addReadyCave();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cave), null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

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

    @Test
    @DisplayName("The first player to accept the payment prevents mana without asking the other player")
    void firstPlayerPaymentEndsTheChoiceSequence() {
        Permanent cave = addReadyCave();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(cave);
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playersWhoTappedLandForManaThisTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Can be activated during the opponent's turn and asks that active player first")
    void canBeActivatedDuringOpponentsTurn() {
        Permanent cave = addReadyCave(player2);
        harness.passPriority(player2);

        activate(cave);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private Permanent addReadyCave() {
        return addReadyCave(player1);
    }

    private Permanent addReadyCave(Player activePlayer) {
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return cave;
    }

    private void activate(Permanent cave) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cave), null, null);
        harness.passBothPriorities();
    }
}
