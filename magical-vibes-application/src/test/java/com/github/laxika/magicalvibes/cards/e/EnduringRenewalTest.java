package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CaribouRange;
import com.github.laxika.magicalvibes.cards.e.ElvishHealer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({EnduringRenewal.class, ElvishHealer.class, BalduvianBears.class, Forest.class, Plains.class})
class EnduringRenewalTest extends BaseCardTest {

    // ===== Hand revealed =====

    @Test
    @DisplayName("Opponent sees the controller's hand; controller does not see the opponent's")
    void onlyControllerHandRevealed() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, List.of(new ElvishHealer()));
        harness.setHand(player2, List.of(new BalduvianBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Elvish Healer"));

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Balduvian Bears"));
    }

    // ===== Draw replacement =====

    @Test
    @DisplayName("Revealed creature card goes to the graveyard instead of being drawn")
    void revealedCreatureGoesToGraveyard() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BalduvianBears(), new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertNotInHand(player1, "Balduvian Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Revealed non-creature is drawn normally")
    void revealedNonCreatureIsDrawn() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Plains(), new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Does not replace an opponent's draw")
    void opponentDrawIsNotReplaced() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>(List.of(new BalduvianBears(), new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Empty library reveal does not lose the game")
    void emptyLibraryDoesNotLose() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setLibrary(player1, new ArrayList<>());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gameLogContains("reveals no cards")).isTrue();
    }

    @Test
    @DisplayName("Creature milled by the draw replacement is not returned to hand")
    void milledCreatureNotReturnedToHand() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BalduvianBears())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertNotInHand(player1, "Balduvian Bears");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Death return =====

    @Test
    @DisplayName("A creature put into the graveyard from the battlefield returns to hand")
    void creatureDiesReturnsToHand() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Balduvian Bears");
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Returns a creature owned by the controller when an opponent controls it at death")
    void creatureOwnedByControllerReturnsWhenOpponentControlsIt() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.stolenCreatures.put(bears.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Balduvian Bears");
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Does not trigger for a creature put into an opponent's graveyard")
    void creaturePutIntoOpponentsGraveyardDoesNotTrigger() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertNotInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Return fizzles if the creature leaves the graveyard in response")
    void returnFizzlesIfRemovedFromGraveyard() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isNotEmpty();
        Card dead = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Balduvian Bears"))
                .findFirst()
                .orElseThrow();
        gd.playerGraveyards.get(player1.getId()).remove(dead);
        gd.addToExile(player1.getId(), dead);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Balduvian Bears");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @CardUsed({CaribouRange.class})
    @DisplayName("A creature token ceases to exist before the return trigger resolves")
    void tokenCreatureIsNotReturnedToHand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent range = harness.addToBattlefieldAndReturn(player1, new CaribouRange());
        harness.addToBattlefield(player1, new EnduringRenewal());
        range.setAttachedTo(forest.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Caribou");
        String tokenId = token.getCard().getId().toString();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));

        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().toString().equals(tokenId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().toString().equals(tokenId));
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
