package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

@CardUsed({UndeadButler.class, Forest.class, GrizzlyBears.class, HolyDay.class})
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
        Card target = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(target, nonCreature));
        Card butlerCard = putButlerOnBattlefield();

        destroyButler();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).contains(target.getId()).doesNotContain(nonCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(butlerCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()))
                .noneMatch(card -> card.getId().equals(butlerCard.getId()));
    }

    @Test
    @DisplayName("Dies, decline may: both cards stay in the graveyard")
    void diesDeclineMayLeavesCardsInGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Card butlerCard = putButlerOnBattlefield();

        destroyButler();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()))
                .anyMatch(card -> card.getId().equals(butlerCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(butlerCard.getId()));
    }

    private Card putButlerOnBattlefield() {
        harness.addToBattlefield(player1, new UndeadButler());
        Permanent butler = gd.playerBattlefields.get(player1.getId()).getFirst();
        return butler.getCard();
    }

    private void destroyButler() {
        Permanent butler = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, butler));
    }
}
