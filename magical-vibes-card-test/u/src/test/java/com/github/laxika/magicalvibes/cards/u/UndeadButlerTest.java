package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadButler.class, Forest.class, DoomedDissenter.class, Abrade.class})
class UndeadButlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from its controller's library")
    void etbMillsThree() {
        Card top1 = new Forest();
        Card top2 = new Forest();
        Card top3 = new Forest();
        Card top4 = new Forest();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4));
        harness.setHand(player1, List.of(new UndeadButler()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(top1, top2, top3);
    }

    @Test
    @DisplayName("Dies, accept may: exiles itself and returns a target creature card")
    void diesAcceptMayReturnsCreature() {
        Card target = new DoomedDissenter();
        Card nonCreature = new Abrade();
        Card opposingCreature = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(target, nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        Card butlerCard = putButlerOnBattlefield();

        destroyButler();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).contains(target.getId())
                .doesNotContain(nonCreature.getId(), butlerCard.getId(), opposingCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(butlerCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()))
                .noneMatch(card -> card.getId().equals(butlerCard.getId()));
    }

    @Test
    @DisplayName("Dies, decline may: both cards stay in the graveyard")
    void diesDeclineMayLeavesCardsInGraveyard() {
        Card target = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(target));
        Card butlerCard = putButlerOnBattlefield();

        destroyButler();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()))
                .anyMatch(card -> card.getId().equals(butlerCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(butlerCard.getId()));
    }

    @Test
    @DisplayName("ETB mills the remaining cards when the library has fewer than three")
    void etbMillsShortLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card opposingCard = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(new UndeadButler()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingCard);
    }

    @Test
    @DisplayName("May exile itself even when no creature remains to return")
    void mayExileWithoutReturnTarget() {
        harness.setGraveyard(player1, List.of(new Abrade()));
        Card butlerCard = putButlerOnBattlefield();

        destroyButler();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(butlerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(butlerCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(butlerCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot return a creature if the source has left the graveyard before resolution")
    void noReturnWhenSourceCannotBeExiled() {
        Card target = new DoomedDissenter();
        harness.setGraveyard(player1, List.of(target));
        putButlerOnBattlefield();

        destroyButler();
        harness.setGraveyard(player1, List.of(target));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Card putButlerOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new UndeadButler()).getCard();
    }

    private void destroyButler() {
        Permanent butler = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, butler));
    }
}
