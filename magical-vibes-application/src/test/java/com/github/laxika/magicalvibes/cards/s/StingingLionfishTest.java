package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingingLionfish.class, Shock.class, Forest.class, GrizzlyBears.class})
class StingingLionfishTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell during an opponent's turn triggers the may ability")
    void firstSpellDuringOpponentsTurnTriggers() {
        addLionfishAndEnterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting taps or untaps a target nonland permanent")
    void acceptsAndTogglesNonlandPermanent() {
        addLionfishAndEnterOpponentsTurn();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger cannot target a land")
    void cannotTargetLand() {
        addLionfishAndEnterOpponentsTurn();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Only the first spell during an opponent's turn triggers")
    void onlyFirstSpellTriggers() {
        addLionfishAndEnterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting during your own turn does not trigger")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new StingingLionfish());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void addLionfishAndEnterOpponentsTurn() {
        harness.addToBattlefield(player1, new StingingLionfish());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
