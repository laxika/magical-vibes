package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DismissiveDenial.class, JhovallRider.class, Forest.class, GrizzlyBears.class})
class DismissiveDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell")
    void countersTargetSpell() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        DismissiveDenial denial = new DismissiveDenial();
        harness.setHand(player2, List.of(denial));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(rider.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(rider.getId()));
    }

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new DismissiveDenial()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dismissive Denial");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.hasType(CardType.LAND)).isTrue())
                .satisfies(card -> assertThat(card.getSupertypes()).contains(CardSupertype.BASIC));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
