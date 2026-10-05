package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiresToll.class, GrizzlyBears.class, HillGiant.class, Swamp.class, TamiyoCollectorOfTales.class})
class MiresTollTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castMiresToll() {
        harness.setHand(player1, List.of(new MiresToll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Target reveals one card per Swamp and the controller chooses the discard")
    void revealsCardsEqualToSwampsAndDiscardsChosenCard() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));

        castMiresToll();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Swamps, Mire's Toll reveals and discards nothing")
    void noSwampsDoesNothing() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castMiresToll();

        assertThat(activeChoice()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mire's Toll cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MiresToll()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("A hand smaller than the Swamp count is revealed in full and only one card is discarded")
    void revealsEntireSmallHand() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        harness.setHand(player2, List.of(first, second));

        castMiresToll();

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().revealedCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(activeChoice()).isNull();
    }

    @Test
    @DisplayName("An empty target hand creates no reveal or discard interaction")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player2, List.of());

        castMiresToll();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(activeChoice()).isNull();
    }

    @Test
    @DisplayName("An opponent's Swamps do not increase the reveal count")
    void countsOnlyControllersSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Swamp hidden = new Swamp();
        Swamp revealed = new Swamp();
        harness.setHand(player2, List.of(hidden, revealed));

        castMiresToll();

        assertThat(activeChoice().revealStage()).isTrue();
        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 1);
        assertThat(activeChoice().revealedCardIds()).containsExactly(revealed.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hidden);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("The Swamp count is determined when the spell resolves")
    void countsSwampsAtResolution() {
        harness.setHand(player1, List.of(new MiresToll()));
        Swamp discarded = new Swamp();
        harness.setHand(player2, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Swamp());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("The caster can target themselves and choose a card to discard")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new Swamp());
        Swamp retained = new Swamp();
        Swamp discarded = new Swamp();
        harness.setHand(player1, List.of(new MiresToll(), retained, discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(activeChoice()).isNull();
    }

    @Test
    @DisplayName("Tamiyo prevents the discard caused by an opponent's Mire's Toll")
    void cannotDiscardThroughTamiyo() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new TamiyoCollectorOfTales());
        Swamp protectedCard = new Swamp();
        harness.setHand(player2, List.of(protectedCard));

        castMiresToll();
        if (activeChoice() != null) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(protectedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(activeChoice()).isNull();
    }
}
