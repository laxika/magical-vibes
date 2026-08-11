package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunspringExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new SunspringExpedition());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining landfall adds no quest counter")
    void decliningLandfallAddsNoCounter() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new SunspringExpedition());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing gains 8 life")
    void removesCountersSacrificesAndGainsLife() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new SunspringExpedition());
        expedition.setCounterCount(CounterType.QUEST, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 8);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The life ability requires three quest counters")
    void requiresThreeQuestCounters() {
        harness.addToBattlefield(player1, new SunspringExpedition());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
