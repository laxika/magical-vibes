package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InducedAmnesia.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class InducedAmnesiaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player exiles their hand face down and draws the same number")
    void exilesHandFaceDownAndDrawsThatMany() {
        Card exiledFirst = new GrizzlyBears();
        Card exiledSecond = new LlanowarElves();
        harness.setHand(player2, new ArrayList<>(List.of(exiledFirst, exiledSecond)));
        harness.setHand(player1, new ArrayList<>(List.of(new InducedAmnesia())));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Forest"));
        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> amnesia.getId().equals(entry.sourcePermanentId()))
                .toList();
        assertThat(exiled).hasSize(2)
                .allMatch(entry -> entry.faceDown() && entry.ownerId().equals(player2.getId()));
        assertThat(exiled).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(exiledFirst.getId(), exiledSecond.getId());
    }

    @Test
    @DisplayName("When Induced Amnesia is put into a graveyard, it returns its exiled cards to their owners' hands")
    void returnsExiledCardsWhenPutIntoGraveyard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(exiledCard)));
        harness.setHand(player1, new ArrayList<>(List.of(new InducedAmnesia())));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(exiledCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, amnesia));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.exiledCards).noneMatch(entry -> amnesia.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Targeting yourself replaces only the cards remaining after casting")
    void canTargetController() {
        Card original = new Forest();
        Card replacement = new Forest();
        harness.setHand(player1, List.of(new InducedAmnesia(), original));
        harness.setLibrary(player1, List.of(replacement));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(replacement);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(original);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, amnesia));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(original, replacement);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand causes no cards to be drawn or exiled")
    void emptyHandDrawsNothing() {
        Card topCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new InducedAmnesia()));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves still exiles the hand indefinitely")
    void leavingBeforeEnterTriggerResolvesDoesNotStopExile() {
        Card original = new Forest();
        Card replacement = new Forest();
        harness.setHand(player2, List.of(original));
        harness.setHand(player1, List.of(new InducedAmnesia()));
        harness.setLibrary(player2, List.of(replacement));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, amnesia));

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(original);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(replacement);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(original);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Bouncing and recasting Induced Amnesia does not link it to the old exiled hand")
    void returningSourceToHandDoesNotReturnExiledCards() {
        Card original = new Forest();
        Card replacement = new Forest();
        harness.setHand(player2, List.of(original));
        harness.setHand(player1, List.of(new InducedAmnesia()));
        harness.setLibrary(player2, List.of(replacement));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, amnesia));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(amnesia.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(replacement);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(original);

        Card secondReplacement = new Forest();
        harness.setLibrary(player2, List.of(secondReplacement));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent recastAmnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(recastAmnesia.getId()).isNotEqualTo(amnesia.getId());
        assertThat(gd.getCardsExiledByPermanent(recastAmnesia.getId())).containsExactly(replacement);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recastAmnesia));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(replacement, secondReplacement);
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(original);
        assertThat(gd.getCardsExiledByPermanent(recastAmnesia.getId())).isEmpty();
    }

}
