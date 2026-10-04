package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GateOfTheBlackDragon;
import com.github.laxika.magicalvibes.cards.g.GateToManorborn;
import com.github.laxika.magicalvibes.cards.g.GateToSeatower;
import com.github.laxika.magicalvibes.cards.g.GateToTheCitadel;
import com.github.laxika.magicalvibes.cards.g.GateToTumbledown;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FollowTheTracks.class, GateOfTheBlackDragon.class, GateToManorborn.class,
        GateToSeatower.class, GateToTheCitadel.class, GateToTumbledown.class})
class FollowTheTracksTest extends BaseCardTest {

    @Test
    void castingFollowTheTracksResolves() {
        harness.castFromHand(player1, new FollowTheTracks(), "{2}{G}");
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.assertInGraveyard(player1, "Follow the Tracks");
    }

    @Test
    void canChooseFromTheEntireFiveCardSpellbook() {
        harness.castFromHand(player1, new FollowTheTracks(), "{2}{G}");
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).extracting(Card::getName).containsExactlyInAnyOrder(
                "Gate to the Citadel", "Gate to Seatower", "Gate of the Black Dragon",
                "Gate to Tumbledown", "Gate to Manorborn");
    }

    @Test
    void conjuresExactlyOneNontokenGateOntoTheBattlefieldTapped() {
        harness.castFromHand(player1, new FollowTheTracks(), "{2}{G}");
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card chosen = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, chosen.getName()).isTapped()).isTrue();
        assertThat(findPermanent(player1, chosen.getName()).getCard().isToken()).isFalse();
        harness.assertNotInHand(player1, chosen.getName());
        harness.assertNotInGraveyard(player1, chosen.getName());
        harness.assertInGraveyard(player1, "Follow the Tracks");
    }

    @Test
    void conjuringAGateDoesNotUseOrRequireALandPlay() {
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.castFromHand(player1, new FollowTheTracks(), "{2}{G}");
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card chosen = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.assertOnBattlefield(player1, chosen.getName());
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
