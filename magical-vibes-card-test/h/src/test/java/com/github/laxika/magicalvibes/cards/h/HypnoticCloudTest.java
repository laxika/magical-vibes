package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypnoticCloud.class, Forest.class})
class HypnoticCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, target player discards one card")
    void discardsOneWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With kicker, target player discards three cards instead")
    void discardsThreeWithKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target the casting player")
    void canTargetController() {
        harness.setHand(player1, List.of(new HypnoticCloud(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new Forest());
        UUID permanentId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    @DisplayName("Kicked spell discards the entire hand when fewer than three cards remain")
    void kickedSpellDiscardsShortHand() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hypnotic Cloud");
    }

    @Test
    @DisplayName("Kicked spell resolves normally against an empty hand")
    void kickedSpellResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hypnotic Cloud");
    }

    @Test
    @DisplayName("The targeted player chooses which card to discard")
    void targetedPlayerChoosesDiscard() {
        Forest retained = new Forest();
        HypnoticCloud discarded = new HypnoticCloud();
        harness.setHand(player2, List.of(retained, discarded));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Kicker cannot be paid with only five mana")
    void rejectsUnaffordableKicker() {
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, player2.getId(), null,
                List.of(), List.of(), false, null, null, List.of(), null, List.of(), true))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Hypnotic Cloud");
        assertThat(gd.stack).isEmpty();
    }
}
