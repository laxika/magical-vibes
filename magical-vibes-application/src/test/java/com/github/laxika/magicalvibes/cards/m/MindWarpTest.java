package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbyssalSpecter;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindWarp.class, DarkRitual.class, Forest.class, GiantGrowth.class})
class MindWarpTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with the chosen X value and target")
    void castingPutsItOnStack() {
        MindWarp mindWarp = new MindWarp();
        harness.setHand(player1, List.of(mindWarp));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving with X=2 prompts the caster to choose two cards from the hand")
    void promptsForTwoChoices() {
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        Card darkRitual = new DarkRitual();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth, darkRitual)));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);
        assertThat(choice.discardMode()).isTrue();
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Looking at the target hand does not publicly reveal its cards")
    void lookingAtHandDoesNotPubliclyRevealCards() {
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth)));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(log -> log.plainText()))
                .noneMatch(log -> log.contains(forest.getName()) || log.contains(giantGrowth.getName()));
    }

    @Test
    @DisplayName("Choosing X cards discards exactly those cards to the target's graveyard")
    void choosingCardsDiscardsThem() {
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        Card darkRitual = new DarkRitual();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth, darkRitual)));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(darkRitual);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(forest, giantGrowth);
    }

    @Test
    @DisplayName("X greater than hand size discards the entire hand")
    void xGreaterThanHandDiscardsAll() {
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth)));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(forest, giantGrowth);
    }

    @Test
    @DisplayName("X=0 discards nothing and prompts no choice")
    void xZeroDiscardsNothing() {
        MindWarp mindWarp = new MindWarp();
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth)));
        harness.setHand(player1, List.of(mindWarp));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, giantGrowth);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mindWarp);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=0 still lets the caster look at the target's hand")
    void xZeroStillLooksAtTargetHand() {
        Card forest = new Forest();
        harness.setHand(player2, List.of(forest));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.gameLog)
                .anyMatch(log -> log.plainText().contains("looks at " + player2.getUsername() + "'s hand."));
        assertThat(harness.getConn1().getMessagesContaining(forest.getName())).isNotEmpty();
    }

    @Test
    @DisplayName("Targeting a player with an empty hand does nothing")
    void emptyHandDoesNothing() {
        MindWarp mindWarp = new MindWarp();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(mindWarp));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mindWarp);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        MindWarp mindWarp = new MindWarp();
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        harness.setHand(player1, new ArrayList<>(List.of(mindWarp, forest, giantGrowth)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(giantGrowth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(mindWarp, forest);
    }

    @Test
    @CardUsed(AbyssalSpecter.class)
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new AbyssalSpecter());
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card forest = new Forest();
        Card giantGrowth = new GiantGrowth();
        harness.setHand(player2, new ArrayList<>(List.of(forest, giantGrowth)));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, giantGrowth);
    }

    @Test
    @CardUsed(Dodecapod.class)
    @DisplayName("A self-targeted discard does not use an opponent-caused discard replacement")
    void selfTargetedDiscardDoesNotUseOpponentReplacement() {
        MindWarp mindWarp = new MindWarp();
        Dodecapod dodecapod = new Dodecapod();
        harness.setHand(player1, new ArrayList<>(List.of(mindWarp, dodecapod)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == dodecapod);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dodecapod);
    }

    @Test
    @DisplayName("Mind Warp goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        MindWarp mindWarp = new MindWarp();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(forest)));
        harness.setHand(player1, List.of(mindWarp));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mindWarp);
    }
}
