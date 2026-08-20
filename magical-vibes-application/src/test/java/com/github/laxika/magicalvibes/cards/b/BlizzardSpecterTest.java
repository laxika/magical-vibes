package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlizzardSpecter.class, GrizzlyBears.class})
class BlizzardSpecterTest extends BaseCardTest {

    private static final String RETURN_MODE = "That player returns a permanent they control to its owner's hand.";
    private static final String DISCARD_MODE = "That player discards a card.";

    @Test
    @DisplayName("Combat damage mode returns a permanent controlled by the damaged player")
    void returnsDamagedPlayersPermanent() {
        Permanent specter = addReadyCreature(player1, new BlizzardSpecter());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player2, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage discard mode makes the damaged player discard")
    void discardsDamagedPlayersCard() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent specter = addReadyCreature(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A blocked Blizzard Specter does not trigger")
    void blockedSpecterDoesNotTrigger() {
        GameData gameData = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addReadyCreature(player1, new BlizzardSpecter());
        specter.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gameData.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
