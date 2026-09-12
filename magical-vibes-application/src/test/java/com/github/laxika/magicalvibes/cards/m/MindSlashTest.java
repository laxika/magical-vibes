package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NobleStand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindSlash.class, Mossdog.class, NobleStand.class})
class MindSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature reveals opponent's hand and discards the chosen card")
    void sacrificeRevealsHandAndDiscardsChosenCard() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of(new Mossdog(), new NobleStand()));

        harness.activateAbility(player1, index, null, player2.getId());

        // The lone creature is auto-sacrificed as the cost.
        harness.assertInGraveyard(player1, "Mossdog");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Noble Stand");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Mossdog");
    }

    @Test
    @DisplayName("Cannot activate at sorcery speed during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new Mossdog());
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
        prepareSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

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

    @Test
    @DisplayName("Cannot activate without the black mana in its cost")
    void cannotActivateWithoutBlackMana() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new Mossdog());
        prepareSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player1, "Mossdog");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent as the activation cost")
    void cannotSacrificeNoncreaturePermanent() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new NobleStand());
        prepareSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player1, "Noble Stand");
    }

    @Test
    @DisplayName("Resolves without a discard when the opponent's hand is empty")
    void resolvesWithEmptyOpponentHand() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private int setupMindSlashWithCreature(Player player) {
        int index = addMindSlash(player);
        harness.addToBattlefield(player, new Mossdog());
        prepareSorcerySpeedActivation(player);
        harness.addMana(player, ManaColor.BLACK, 1);
        return index;
    }

    private int addMindSlash(Player player) {
        harness.addToBattlefield(player, new MindSlash());
        return gd.playerBattlefields.get(player.getId()).size() - 1;
    }

    private void prepareSorcerySpeedActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
