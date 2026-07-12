package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature reveals opponent's hand and discards the chosen card")
    void sacrificeRevealsHandAndDiscardsChosenCard() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.activateAbility(player1, index, null, player2.getId());

        // The lone creature is auto-sacrificed as the cost.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate at sorcery speed during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        int index = addMindSlash(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target self")
    void cannotTargetSelf() {
        int index = setupMindSlashWithCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int setupMindSlashWithCreature(Player player) {
        int index = addMindSlash(player);
        harness.addToBattlefield(player, new GrizzlyBears());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.clearPriorityPassed();
        return index;
    }

    private int addMindSlash(Player player) {
        Permanent permanent = new Permanent(new MindSlash());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
