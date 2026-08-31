package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoiraOfTheGhitu.class, GrizzlyBears.class, Mountain.class})
class JhoiraOfTheGhituTest extends BaseCardTest {

    @Test
    void exilesNonlandCardAndPutsFourTimeCountersOnIt() {
        Permanent jhoira = jhoira();
        GrizzlyBears first = new GrizzlyBears();

        harness.setHand(player1, List.of(first));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate(jhoira, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first);
        assertThat(gd.exiledCardTimeCounters).containsEntry(first.getId(), 4);
    }

    @Test
    void separateActivationsSuspendTheCardPaidForEachActivation() {
        Permanent jhoira = jhoira();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();

        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(first.getId(), 4)
                .containsEntry(second.getId(), 4);
    }

    @Test
    void lastTimeCounterOffersAFreeCastWithHaste() {
        Permanent jhoira = jhoira();
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate(jhoira, 0);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent castPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, castPermanent, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotExileALandAsTheActivationCost() {
        Permanent jhoira = jhoira();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent jhoira() {
        return harness.addToBattlefieldAndReturn(player1, new JhoiraOfTheGhitu());
    }

    private void activate(Permanent jhoira, int handCardIndex) {
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, handCardIndex);
        harness.passBothPriorities();
    }
}
