package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FirdochCore;
import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.cards.s.SelhoffEntomber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AimForTheHead.class, PersistentSpecimen.class, SelhoffEntomber.class, FirdochCore.class})
class AimForTheHeadTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode exiles a target Zombie")
    void exileModeExilesTargetZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new SelhoffEntomber());
        cast(0, zombie.getId());

        harness.assertNotOnBattlefield(player2, "Selhoff Entomber");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Selhoff Entomber"));
    }

    @Test
    @DisplayName("Hand mode makes the target opponent exile two cards")
    void handModeExilesTwoCardsFromTargetOpponentsHand() {
        harness.setHand(player2, List.of(new PersistentSpecimen(), new PersistentSpecimen()));
        cast(1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exile mode cannot target a non-Zombie creature")
    void exileModeRequiresZombie() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        assertThatThrownBy(() -> cast(0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Zombie");
    }

    @Test
    @DisplayName("Hand mode cannot target its controller")
    void handModeRequiresOpponent() {
        assertThatThrownBy(() -> cast(1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void exileModeCanExileOwnZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new SelhoffEntomber());
        cast(0, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Selhoff Entomber");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zombie.getCard());
    }

    @Test
    void exileModeCanExileNoncreatureZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new FirdochCore());
        cast(0, zombie.getId());

        harness.assertNotOnBattlefield(player2, "Firdoch Core");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(zombie.getCard());
    }

    @Test
    void handModeExilesOnlyCardWhenOpponentHasOne() {
        PersistentSpecimen card = new PersistentSpecimen();
        harness.setHand(player2, List.of(card));
        cast(1, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void handModeResolvesWithEmptyHand() {
        harness.setHand(player2, List.of());
        cast(1, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aim for the Head");
    }

    @Test
    void opponentChoosesWhichTwoCardsToExile() {
        PersistentSpecimen retained = new PersistentSpecimen();
        SelhoffEntomber first = new SelhoffEntomber();
        PersistentSpecimen second = new PersistentSpecimen();
        harness.setHand(player2, List.of(retained, first, second));
        cast(1, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AimForTheHead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, mode, targetId);
    }
}
