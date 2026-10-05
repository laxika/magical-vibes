package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GristleGrinner;
import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrOfBones.class, GristleGrinner.class, KjeldoranOutrider.class, KrovikanScoundrel.class})
class MartyrOfBonesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals black cards, sacrifices itself, and exiles up to X cards from one graveyard")
    void revealsAndExilesCards() {
        KrovikanScoundrel firstBlackCard = new KrovikanScoundrel();
        GristleGrinner secondBlackCard = new GristleGrinner();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));

        Card firstGraveyardCard = new KrovikanScoundrel();
        Card secondGraveyardCard = new GristleGrinner();
        Card untouchedCard = new KjeldoranOutrider();
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
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBlackCard, secondBlackCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouchedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstGraveyardCard, secondGraveyardCard);
    }

    @Test
    @DisplayName("Allows choosing zero cards when X is zero")
    void allowsZeroTargetsWhenXIsZero() {
        KrovikanScoundrel blackCard = new KrovikanScoundrel();
        harness.setHand(player1, List.of(blackCard));
        Card graveyardCard = new GristleGrinner();
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
    @DisplayName("Allows zero targets when X is positive")
    void allowsZeroTargetsWhenXIsPositive() {
        KrovikanScoundrel blackCard = new KrovikanScoundrel();
        harness.setHand(player1, List.of(blackCard));
        Card graveyardCard = new GristleGrinner();
        harness.setGraveyard(player2, List.of(graveyardCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate(martyr, 1, List.of());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a nonblack card in the reveal selection")
    void rejectsNonBlackCardInRevealSelection() {
        KrovikanScoundrel blackCard = new KrovikanScoundrel();
        KjeldoranOutrider nonBlackCard = new KjeldoranOutrider();
        harness.setHand(player1, List.of(blackCard, nonBlackCard));

        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate(martyr, 1, List.of());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonBlackCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card ID");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard, nonBlackCard);
    }

    @Test
    @DisplayName("Cannot reveal more black cards than are in hand")
    void cannotRevealMoreBlackCardsThanInHand() {
        KjeldoranOutrider nonBlackCard = new KjeldoranOutrider();
        harness.setHand(player1, List.of(nonBlackCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(martyr, 1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching cards in hand");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonBlackCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects more than X graveyard targets before paying costs")
    void rejectsMoreThanXTargets() {
        KrovikanScoundrel blackCard = new KrovikanScoundrel();
        harness.setHand(player1, List.of(blackCard));
        Card firstGraveyardCard = new KrovikanScoundrel();
        Card secondGraveyardCard = new GristleGrinner();
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
        KrovikanScoundrel firstBlackCard = new KrovikanScoundrel();
        GristleGrinner secondBlackCard = new GristleGrinner();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        Card ownGraveyardCard = new KrovikanScoundrel();
        Card opponentGraveyardCard = new GristleGrinner();
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

    @Test
    @DisplayName("A tapped, summoning-sick Martyr can exile fewer than X cards from its controller's graveyard")
    void exilesFewerThanXFromOwnGraveyardWithoutTapCost() {
        Card firstBlackCard = new KrovikanScoundrel();
        Card secondBlackCard = new GristleGrinner();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        Card target = new KjeldoranOutrider();
        Card untouched = new KrovikanScoundrel();
        harness.setGraveyard(player1, List.of(target, untouched));
        Permanent martyr = harness.addToBattlefieldAndReturn(player1, new MartyrOfBones());
        martyr.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(martyr, 2, List.of(target.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard(), target, untouched);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(untouched, martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBlackCard, secondBlackCard);
    }

    @Test
    @DisplayName("Exiles the remaining legal target when another target leaves the graveyard")
    void resolvesWithOneTargetMissing() {
        Card firstBlackCard = new KrovikanScoundrel();
        Card secondBlackCard = new GristleGrinner();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        Card removedTarget = new KrovikanScoundrel();
        Card remainingTarget = new KjeldoranOutrider();
        harness.setGraveyard(player2, List.of(removedTarget, remainingTarget));
        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(martyr, 2, List.of(removedTarget.getId(), remainingTarget.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));
        harness.setGraveyard(player2, List.of(remainingTarget));
        harness.setHand(player2, List.of(removedTarget));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remainingTarget);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removedTarget);
    }

    @Test
    @DisplayName("Cannot choose the same graveyard card twice")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new KrovikanScoundrel(), new GristleGrinner()));
        Card target = new KjeldoranOutrider();
        harness.setGraveyard(player2, List.of(target));
        Permanent martyr = addCreatureReady(player1, new MartyrOfBones());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activate(martyr, 2, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same card twice");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    private void activate(Permanent martyr, int xValue, List<UUID> targetIds) {
        harness.forceActivePlayer(player1);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(martyr);
        gs.activateAbility(gd, player1, permanentIndex, 0, xValue, null, Zone.GRAVEYARD, targetIds);
    }
}
