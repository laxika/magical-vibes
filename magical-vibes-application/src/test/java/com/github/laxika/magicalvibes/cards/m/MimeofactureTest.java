package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mimeofacture.class, GrizzlyBears.class, Divination.class, Forest.class})
class MimeofactureTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards with the target permanent's name from that player's library")
    void offersCardsWithTargetPermanentName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears matching = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                matching, new Divination(), new Forest()));

        castMimeofacture(target, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst().getId()).isEqualTo(matching.getId());
    }

    @Test
    @DisplayName("Puts the chosen same-name card onto the caster's battlefield")
    void putsChosenCardUnderCasterControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears found = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(found);

        castMimeofacture(target, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(found.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Replicate creates an additional same-name search")
    void replicateCreatesAdditionalSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(first, second));

        castMimeofacture(target, List.of("{3}{U}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mimeofacture()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castMimeofacture(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Mimeofacture()));
        harness.addMana(player1, ManaColor.BLUE, 4 + replicatePayments.size() * 4);
        harness.castSorceryWithRepeatedCosts(player1, 0, replicatePayments, List.of(target.getId()));
    }
}
