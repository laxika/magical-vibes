package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IorRuinExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining landfall adds no quest counter")
    void decliningLandfallAddsNoQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing draws two cards")
    void removesCountersSacrificesAndDrawsTwoCards() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    @DisplayName("The ability cannot be activated without three quest counters")
    void cannotActivateWithoutThreeQuestCounters() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addExpedition() {
        return harness.addToBattlefieldAndReturn(player1, new IorRuinExpedition());
    }
}
