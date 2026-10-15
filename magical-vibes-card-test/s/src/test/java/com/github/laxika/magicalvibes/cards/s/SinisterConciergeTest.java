package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinisterConcierge.class, GrizzlyBears.class, Murder.class})
class SinisterConciergeTest extends BaseCardTest {

    @Test
    void mayExileItAndUpToOneTargetCreatureWithSuspend() {
        SinisterConcierge conciergeCard = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, conciergeCard);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetCard = target.getCard();

        destroy(concierge);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(conciergeCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(targetCard);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(conciergeCard.getId(), 3)
                .containsEntry(targetCard.getId(), 3);
    }

    @Test
    void decliningMayAbilityLeavesConciergeInGraveyardAndTargetOnBattlefield() {
        SinisterConcierge conciergeCard = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, conciergeCard);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetCard = target.getCard();

        destroy(concierge);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conciergeCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(conciergeCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(targetCard);
    }

    @Test
    void mayExileOnlySinisterConciergeWithoutChoosingATarget() {
        SinisterConcierge conciergeCard = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, conciergeCard);

        destroy(concierge);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(conciergeCard);
        assertThat(gd.exiledCardTimeCounters).containsEntry(conciergeCard.getId(), 3);
    }

    @Test
    void illegalTargetPreventsExilingConcierge() {
        SinisterConcierge card = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, card);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroy(concierge);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        destroy(target);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void suspendedCardsLoseCountersOnlyOnTheirOwnersUpkeeps() {
        SinisterConcierge card = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, card);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroy(concierge);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(card.getId(), 2)
                .containsEntry(target.getCard().getId(), 3);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(card.getId(), 2)
                .containsEntry(target.getCard().getId(), 2);
    }

    @Test
    void lastCounterAllowsFreeCastWithHaste() {
        SinisterConcierge card = suspendConciergeWithoutTarget();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Sinister Concierge");
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void decliningFreeCastLeavesConciergeExiledWithoutFurtherTriggers() {
        SinisterConcierge card = suspendConciergeWithoutTarget();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        advanceToUpkeep(player1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Sinister Concierge");
    }

    private SinisterConcierge suspendConciergeWithoutTarget() {
        SinisterConcierge card = new SinisterConcierge();
        Permanent concierge = harness.addToBattlefieldAndReturn(player1, card);
        destroy(concierge);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        return card;
    }

    private void destroy(Permanent target) {
        harness.setHand(player2, java.util.List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
