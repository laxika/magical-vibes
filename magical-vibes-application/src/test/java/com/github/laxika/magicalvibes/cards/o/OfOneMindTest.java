package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OfOneMind.class, CheckpointOfficer.class, AlmightyBrushwagg.class})
class OfOneMindTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {U} when you control a Human creature and a non-Human creature")
    void costsOneBlueWithHumanAndNonHumanCreature() {
        harness.addToBattlefield(player1, new CheckpointOfficer());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a Human creature")
    void doesNotGetReductionWithOnlyNonHumanCreature() {
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a non-Human creature")
    void doesNotGetReductionWithOnlyHumanCreature() {
        harness.addToBattlefield(player1, new CheckpointOfficer());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Of One Mind draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.addToBattlefield(player1, new CheckpointOfficer());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Pays the full cost and draws two cards without any creatures")
    void fullCostWithoutCreatures() {
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's non-Human creature does not enable the discount")
    void opponentNonHumanDoesNotEnableDiscount() {
        harness.addToBattlefield(player1, new CheckpointOfficer());
        harness.addToBattlefield(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Human creature does not enable the discount")
    void opponentHumanDoesNotEnableDiscount() {
        harness.addToBattlefield(player2, new CheckpointOfficer());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discount does not remove the blue mana requirement")
    void discountStillRequiresBlueMana() {
        harness.addToBattlefield(player1, new CheckpointOfficer());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
