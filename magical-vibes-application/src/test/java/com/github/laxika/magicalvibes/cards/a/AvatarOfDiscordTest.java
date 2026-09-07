package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarOfDiscord.class, GrizzlyBears.class})
class AvatarOfDiscordTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding two cards keeps Avatar of Discord")
    void discardingTwoCardsKeepsAvatar() {
        castAvatar(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(firstChoice.remainingCount()).isEqualTo(2);
        assertThat(firstChoice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        PendingInteraction.DiscardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(secondChoice.remainingCount()).isEqualTo(1);
        assertThat(secondChoice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Avatar of Discord");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the discard sacrifices Avatar of Discord")
    void decliningDiscardSacrificesAvatar() {
        castAvatar(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Avatar of Discord");
        harness.assertInGraveyard(player1, "Avatar of Discord");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Fewer than two cards automatically sacrifices Avatar of Discord")
    void fewerThanTwoCardsAutomaticallySacrificesAvatar() {
        castAvatar(List.of(new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Avatar of Discord");
        harness.assertInGraveyard(player1, "Avatar of Discord");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castAvatar(List<Card> hand) {
        harness.setHand(player1, List.of(new AvatarOfDiscord()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.setHand(player1, hand);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
