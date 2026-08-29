package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrOfBonesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals black cards, sacrifices itself, and exiles up to X cards from one graveyard")
    void revealsAndExilesCards() {
        DarkRitual firstBlackCard = new DarkRitual();
        DoomBlade secondBlackCard = new DoomBlade();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));

        Card firstGraveyardCard = new GrizzlyBears();
        Card secondGraveyardCard = new LightningBolt();
        Card untouchedCard = new DarkRitual();
        harness.setGraveyard(player2, List.of(firstGraveyardCard, secondGraveyardCard, untouchedCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate(martyr, 2, List.of(firstGraveyardCard.getId(), secondGraveyardCard.getId()));

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouchedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstGraveyardCard, secondGraveyardCard);
    }

    @Test
    @DisplayName("Allows choosing zero cards when X is zero")
    void allowsZeroTargets() {
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(blackCard));
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate(martyr, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects more than X graveyard targets before paying costs")
    void rejectsMoreThanXTargets() {
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(blackCard));
        Card firstGraveyardCard = new GrizzlyBears();
        Card secondGraveyardCard = new LightningBolt();
        harness.setGraveyard(player2, List.of(firstGraveyardCard, secondGraveyardCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(martyr, 1,
                List.of(firstGraveyardCard.getId(), secondGraveyardCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot target more than 1 cards");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires all targets to come from one graveyard")
    void rejectsTargetsFromDifferentGraveyards() {
        DarkRitual firstBlackCard = new DarkRitual();
        DoomBlade secondBlackCard = new DoomBlade();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        Card ownGraveyardCard = new GrizzlyBears();
        Card opponentGraveyardCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(martyr, 2,
                List.of(ownGraveyardCard.getId(), opponentGraveyardCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void activate(Permanent martyr, int xValue, List<UUID> targetIds) {
        harness.forceActivePlayer(player1);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(martyr);
        gs.activateAbility(gd, player1, permanentIndex, 0, xValue, null, Zone.GRAVEYARD, targetIds);
    }
}
