package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CleverImpersonator;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsEyeSavants.class, CleverImpersonator.class, DisownedAncestor.class})
class DragonsEyeSavantsTest extends BaseCardTest {

    @Test
    void morphRevealsBlueCardAndKeepsItInHand() {
        CleverImpersonator blueCard = new CleverImpersonator();
        harness.setHand(player1, List.of(new DragonsEyeSavants(), blueCard));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Dragon's Eye Savants");
        assertThat(permanent.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blueCard);
        harness.clearMessages();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent), 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blueCard);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Clever Impersonator"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Clever Impersonator"));
    }

    @Test
    void turningFaceUpLooksAtTargetOpponentsHand() {
        CleverImpersonator opponentCard = new CleverImpersonator();
        harness.setHand(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new CleverImpersonator()));
        Permanent permanent = addFaceDownSavants();

        harness.turnFaceUp(player1, 0, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.clearMessages();
        assertThat(gameLogContains("looks at")).isFalse();
        resolveAllTriggers();

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains(opponentCard.getId().toString()));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    void canCastFaceDownWithoutAnotherBlueCard() {
        harness.setHand(player1, List.of(new DragonsEyeSavants()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Dragon's Eye Savants").isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gameLogContains("looks at")).isFalse();
    }

    @Test
    void cannotTurnFaceUpByPayingManaWithoutRevealing() {
        Permanent permanent = addFaceDownSavants();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(permanent.isFaceDown()).isTrue();
    }

    @Test
    void cannotRevealANonblueCardToTurnFaceUp() {
        Permanent permanent = addFaceDownSavants();
        DisownedAncestor nonblueCard = new DisownedAncestor();
        harness.setHand(player1, List.of(nonblueCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(permanent.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonblueCard);
    }

    @Test
    void cannotTargetControllerWithHandLookTrigger() {
        addFaceDownSavants();
        harness.setHand(player1, List.of(new CleverImpersonator()));
        harness.turnFaceUp(player1, 0, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
    }

    @Test
    void canLookAtAnEmptyOpponentsHand() {
        addFaceDownSavants();
        harness.setHand(player1, List.of(new CleverImpersonator()));
        harness.setHand(player2, List.of());
        harness.turnFaceUp(player1, 0, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("empty"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringFaceUpDoesNotLookAtOpponentsHand() {
        harness.setHand(player1, List.of(new DragonsEyeSavants()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dragon's Eye Savants");
        assertThat(gameLogContains("looks at")).isFalse();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFaceDownSavants() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DragonsEyeSavants());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
