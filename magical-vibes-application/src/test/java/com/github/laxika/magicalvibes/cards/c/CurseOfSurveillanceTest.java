package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfSurveillance.class, CurseOfSilence.class})
class CurseOfSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("The Aura resolves attached to its targeted player")
    void resolvesAttachedToTargetPlayer() {
        harness.setHand(player1, List.of(new CurseOfSurveillance()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent surveillance = findPermanent(player1, "Curse of Surveillance");
        assertThat(surveillance.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The enchanted player's upkeep lets the controller choose the other player")
    void choosesPlayersOtherThanTheEnchantedPlayer() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        harness.setLibrary(player1, List.of(new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Each chosen player draws for every Curse attached to the enchanted player")
    void drawsForEachAttachedCurse() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        placeCurse(player1, player2, new CurseOfSilence());
        harness.setLibrary(player1, List.of(new CurseOfSilence(), new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The controller may choose no target players")
    void mayChooseNoPlayers() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("A self-enchanted controller chooses the opponent to draw")
    void selfEnchantedControllerStillChoosesTargets() {
        placeCurse(player1, player1, new CurseOfSurveillance());
        harness.setLibrary(player2, List.of(new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The ability does not trigger during an unenchanted player's upkeep")
    void doesNotTriggerOnOtherPlayersUpkeep() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Curses on the enchanted player count regardless of their controller")
    void countsCursesControlledByEitherPlayerButNotCursesOnOtherPlayers() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        placeCurse(player2, player2, new CurseOfSilence());
        placeCurse(player1, player1, new CurseOfSilence());
        harness.setLibrary(player1, List.of(new CurseOfSilence(), new CurseOfSilence(), new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The number of Curses is checked at resolution")
    void countsCursesAddedAfterTheAbilityTriggers() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        harness.setLibrary(player1, List.of(new CurseOfSilence(), new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        placeCurse(player2, player2, new CurseOfSilence());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("Removing the source does not stop its trigger, and the removed Curse is not counted")
    void resolvesAfterSourceLeavesWithRemainingCurseCount() {
        Permanent surveillance = placeCurse(player1, player2, new CurseOfSurveillance());
        placeCurse(player2, player2, new CurseOfSilence());
        harness.setLibrary(player1, List.of(new CurseOfSilence(), new CurseOfSilence()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, surveillance);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    private Permanent placeCurse(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curse) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, curse);
        permanent.setAttachedTo(enchantedPlayer.getId());
        return permanent;
    }
}
