package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Confessor.class, CephalidLooter.class, CarefulStudy.class})
class ConfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when accepting the trigger after an opponent discards")
    void gainsLifeWhenOpponentDiscards() {
        setUpConfessorAndLooter();
        setUpLooterDiscard(player2);

        resolveLooterDiscard(player2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life when accepting the trigger after the controller discards")
    void gainsLifeWhenControllerDiscards() {
        setUpConfessorAndLooter();
        setUpLooterDiscard(player1);

        resolveLooterDiscard(player1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when declining the trigger")
    void decliningTriggerDoesNotGainLife() {
        setUpConfessorAndLooter();
        setUpLooterDiscard(player2);

        resolveLooterDiscard(player2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Offers life gain once for each card discarded")
    void offersLifeGainForEachCardDiscarded() {
        harness.addToBattlefield(player1, new Confessor());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(
                new CarefulStudy(), new CephalidLooter(), new CephalidLooter()));
        harness.setLibrary(player1, List.of(new CephalidLooter(), new CephalidLooter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    private void setUpConfessorAndLooter() {
        addCreatureReady(player1, new CephalidLooter());
        harness.addToBattlefield(player1, new Confessor());
        harness.setLife(player1, 20);
    }

    private void setUpLooterDiscard(Player discardingPlayer) {
        harness.setHand(discardingPlayer, List.of(new CephalidLooter()));
        harness.setLibrary(discardingPlayer, List.of(new CephalidLooter()));
    }

    private void resolveLooterDiscard(Player discardingPlayer) {
        harness.activateAbility(player1, 0, null, discardingPlayer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(discardingPlayer, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
