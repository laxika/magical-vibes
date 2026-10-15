package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrillBit.class, GruulGuildgate.class, SauroformHybrid.class, GrowthSpiral.class})
class DrillBitTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the target hand and allows choosing a nonland card to discard")
    void choosesNonlandCardToDiscard() {
        harness.setHand(player2, List.of(new GruulGuildgate(), new SauroformHybrid(), new GrowthSpiral()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Sauroform Hybrid");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gruul Guildgate", "Growth Spiral");
    }

    @Test
    @DisplayName("A hand containing only lands has no legal discard choice")
    void onlyLandsCannotBeDiscarded() {
        harness.setHand(player2, List.of(new GruulGuildgate()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gruul Guildgate");
    }

    @Test
    @DisplayName("Spectacle casts Drill Bit for {B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player2, List.of(new SauroformHybrid()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseAnInstantButCannotChooseALandOrDecline() {
        harness.setHand(player2, List.of(new GruulGuildgate(), new GrowthSpiral()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Growth Spiral");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gruul Guildgate");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new DrillBit(), new GrowthSpiral(), new GruulGuildgate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Growth Spiral");
        harness.assertInGraveyard(player1, "Drill Bit");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Gruul Guildgate");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyHandResolvesWithoutAChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Drill Bit");
    }

    @Test
    void controllersLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spectacleStillAllowsTargetingSelfAfterOpponentRegainsLife() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new DrillBit(), new SauroformHybrid()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Sauroform Hybrid");
        harness.assertInGraveyard(player1, "Drill Bit");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
