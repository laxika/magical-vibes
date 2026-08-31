package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BloodletterOfAclazotz;
import com.github.laxika.magicalvibes.cards.e.Evermind;
import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfIth.class, MazeOfIth.class, ScarwoodGoblins.class, BloodletterOfAclazotz.class,
        Evermind.class, PlatinumEmperion.class})
class WandOfIthTest extends BaseCardTest {

    private void readyWand() {
        harness.addToBattlefield(player1, new WandOfIth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateOn(Card card) {
        harness.setHand(player2, List.of(card));
        readyWand();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    private void activateOnWithBloodletter(Card card) {
        harness.setHand(player2, List.of(card));
        readyWand();
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    private void activateOnWithLifeTotalLockedPlayer(Card card) {
        harness.setHand(player2, List.of(card));
        harness.addToBattlefield(player2, new PlatinumEmperion());
        readyWand();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player may pay 1 life to keep a revealed land")
    void paysOneLifeToKeepLand() {
        activateOn(new MazeOfIth());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Maze of Ith");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player may pay the mana value of a revealed nonland card to keep it")
    void paysManaValueToKeepNonland() {
        activateOn(new ScarwoodGoblins());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInHand(player2, "Scarwood Goblins");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Paying life is not doubled by an opponent life-loss replacement effect")
    void payingLifeIsNotDoubledByLifeLossReplacement() {
        activateOnWithBloodletter(new MazeOfIth());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Maze of Ith");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player can pay zero life for a card with no mana cost")
    void canPayZeroLifeWhenLifeTotalCannotChange() {
        Card card = new Evermind();
        activateOnWithLifeTotalLockedPlayer(card);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining discards a revealed nonland card")
    void declineDiscardsNonland() {
        activateOn(new ScarwoodGoblins());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Scarwood Goblins");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A player who cannot pay the mana-value life cost discards automatically")
    void cannotPayDiscardsAutomatically() {
        harness.setLife(player2, 1);
        activateOn(new ScarwoodGoblins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Scarwood Goblins");
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Does nothing when the target player's hand is empty")
    void doesNothingForEmptyHand() {
        harness.setHand(player2, List.of());
        readyWand();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target only a player")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new MazeOfIth());
        readyWand();
        var targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, List.of(new MazeOfIth()));
        harness.addToBattlefield(player1, new WandOfIth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
