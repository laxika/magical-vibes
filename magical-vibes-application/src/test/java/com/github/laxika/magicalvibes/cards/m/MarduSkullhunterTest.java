package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarduSkullhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Mardu Skullhunter enters tapped")
    void entersTapped() {
        castMarduSkullhunter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Raid ETB makes target opponent discard a card")
    void raidMakesOpponentDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Raid ETB does not trigger if you did not attack this turn")
    void raidDoesNotTriggerWithoutAttack() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castMarduSkullhunter();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Raid ETB target choice only offers opponents")
    void targetMustBeOpponent() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Raid ETB does nothing if raid is lost before resolution")
    void raidFizzlesIfLostBeforeResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("raid ability does nothing"));
    }

    private void castMarduSkullhunter() {
        harness.setHand(player1, List.of(new MarduSkullhunter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
    }
}
