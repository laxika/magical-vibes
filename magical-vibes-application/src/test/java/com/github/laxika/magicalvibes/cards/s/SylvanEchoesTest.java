package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.cards.p.PollenLullaby;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanEchoes.class, Forest.class, KithkinGreatheart.class, PollenLullaby.class})
class SylvanEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Won clash: accepting the may draws a card")
    void wonClashDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        // Kithkin Greatheart has mana value 2; Forest has mana value 0, so player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new KithkinGreatheart());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities(); // Resolve the trigger to offer the optional draw.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Won clash: declining the may draws nothing")
    void wonClashDeclineDrawsNothing() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        gd.playerDecks.get(player1.getId()).addFirst(new KithkinGreatheart());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lost clash: no trigger, no draw prompt")
    void lostClashNoTrigger() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        // Forest has mana value 0; Kithkin Greatheart has mana value 2, so player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new KithkinGreatheart());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equal mana values do not trigger Sylvan Echoes")
    void tiedClashDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Each copy offers its own optional draw")
    void multipleCopiesOfferIndependentDraws() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());
        harness.addToBattlefield(player1, new SylvanEchoes());
        harness.setLibrary(player1, List.of(new KithkinGreatheart(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Winning an opponent's clash still offers a draw")
    void winningOpponentInitiatedClashDrawsCard() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SylvanEchoes());
        KithkinGreatheart revealed = new KithkinGreatheart();
        harness.setLibrary(player1, List.of(revealed));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new PollenLullaby()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(revealed);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing uses the library after clash placement choices")
    void drawOccursAfterBottomingWinningCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SylvanEchoes());
        KithkinGreatheart revealed = new KithkinGreatheart();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(revealed, nextCard));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new PollenLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.stack).isEmpty();
    }
}
