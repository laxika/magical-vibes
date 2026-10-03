package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlizzardSpecter.class, BorealDruid.class, BorealGriffin.class})
class BlizzardSpecterTest extends BaseCardTest {

    private static final String RETURN_MODE = "That player returns a permanent they control to its owner's hand.";
    private static final String DISCARD_MODE = "That player discards a card.";

    @Test
    @DisplayName("Combat damage mode returns a permanent controlled by the damaged player")
    void returnsDamagedPlayersPermanent() {
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        Permanent target = addCreatureReady(player2, new BorealDruid());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player2, target.getId());

        harness.assertInHand(player2, "Boreal Druid");
        harness.assertNotOnBattlefield(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Combat damage discard mode makes the damaged player discard")
    void discardsDamagedPlayersCard() {
        harness.setHand(player2, List.of(new BorealDruid()));
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Return mode does nothing when the damaged player controls no permanents")
    void returnModeDoesNothingWithoutPermanent() {
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Blizzard Specter");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A blocked Blizzard Specter does not trigger")
    void blockedSpecterDoesNotTrigger() {
        GameData gameData = harness.getGameData();
        harness.setHand(player2, List.of(new BorealDruid()));

        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorealGriffin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gameData.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses the mode before players can respond to the trigger")
    void choosesModeWhenTriggerGoesOnStack() {
        harness.setHand(player2, List.of(new BorealDruid()));
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.handleListChoice(player1, DISCARD_MODE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Discard mode does nothing when the damaged player's hand is empty")
    void discardModeDoesNothingWithEmptyHand() {
        harness.setHand(player2, List.of());
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Blizzard Specter");
    }

    @Test
    @DisplayName("The damaged player chooses which of their permanents to return")
    void damagedPlayerChoosesPermanent() {
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        Permanent first = addCreatureReady(player2, new BorealDruid());
        Permanent second = addCreatureReady(player2, new BorealDruid());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        harness.assertInHand(player2, "Boreal Druid");
    }
}
