package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
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
    void decliningMayAbilityLeavesBothCardsInTheirGraveyards() {
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

    private void destroy(Permanent target) {
        harness.setHand(player2, java.util.List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
