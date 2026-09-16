package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.c.CateranEnforcer;
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

@CardUsed({Extortion.class, CateranBrute.class, CateranEnforcer.class})
class ExtortionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting targets a player")
    void castingTargetsAPlayer() {
        harness.setHand(player1, List.of(new Extortion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The caster looks at the target hand and chooses up to two cards to discard")
    void choosesUpToTwoCardsToDiscard() {
        Card first = new CateranBrute();
        Card second = new CateranEnforcer();
        Card untouched = new Extortion();
        harness.setHand(player2, new ArrayList<>(List.of(first, second, untouched)));
        harness.setHand(player1, List.of(new Extortion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(2);
        assertThat(choice.discardMode()).isTrue();
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("The caster may choose fewer than two cards")
    void mayChooseFewerThanTwoCards() {
        Card chosen = new CateranBrute();
        Card kept = new CateranEnforcer();
        harness.setHand(player2, new ArrayList<>(List.of(chosen, kept)));
        harness.setHand(player1, List.of(new Extortion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("Looking at the target hand is visible only to the caster")
    void lookingAtHandIsPrivate() {
        Card revealed = new CateranBrute();
        harness.setHand(player2, List.of(revealed));
        harness.setHand(player1, List.of(new Extortion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anySatisfy(message -> assertThat(message).contains(revealed.getId().toString()));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains(revealed.getName()));

        harness.handleCardChosen(player1, 0);
    }

    @Test
    @DisplayName("An empty target hand results in no card choice")
    void emptyTargetHandResultsInNoChoice() {
        Extortion spell = new Extortion();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        Extortion spell = new Extortion();
        Card chosen = new CateranBrute();
        Card kept = new CateranEnforcer();
        harness.setHand(player1, new ArrayList<>(List.of(spell, chosen, kept)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, chosen);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CateranBrute());
        Extortion spell = new Extortion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }
}
