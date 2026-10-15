package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObekaBruteChronologist.class, LightningBolt.class})
class ObekaBruteChronologistTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may accept ending the turn")
    void activePlayerMayEndTheTurn() {
        addCreatureReady(player1, new ObekaBruteChronologist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int turnBefore = gd.turnNumber;
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The active player may decline ending the turn")
    void activePlayerMayDeclineEndingTheTurn() {
        addCreatureReady(player1, new ObekaBruteChronologist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int turnBefore = gd.turnNumber;
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ending your own turn exiles a pending spell without resolving it")
    void endingOwnTurnExilesPendingSpell() {
        addCreatureReady(player1, new ObekaBruteChronologist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bolt);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Obeka, Brute Chronologist");
    }

    @Test
    @DisplayName("Declining leaves the pending spell available to resolve")
    void decliningPreservesPendingSpell() {
        Permanent obeka = addCreatureReady(player1, new ObekaBruteChronologist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(obeka.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bolt);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bolt);
    }

    @Test
    @DisplayName("Cleanup discard happens before marked damage is removed")
    void discardPrecedesDamageRemoval() {
        Permanent obeka = addCreatureReady(player1, new ObekaBruteChronologist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, obeka.getId());
        assertThat(obeka.getMarkedDamage()).isEqualTo(3);
        harness.setHand(player1, IntStream.range(0, 8)
                .mapToObj(i -> (Card) new ObekaBruteChronologist())
                .toList());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(obeka.getMarkedDamage()).isEqualTo(3);
    }
}
