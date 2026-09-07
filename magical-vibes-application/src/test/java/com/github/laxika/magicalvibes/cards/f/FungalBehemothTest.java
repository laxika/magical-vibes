package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FungalBehemoth.class, GrizzlyBears.class})
class FungalBehemothTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualPlusOneCountersOnCreaturesYouControl() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new FungalBehemoth());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(2);

        behemoth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    void suspendUsesChosenXAsTimeCounters() {
        FungalBehemoth card = suspendCard(3);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void suspendXCannotBeZero() {
        FungalBehemoth card = new FungalBehemoth();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void removingTimeCounterMayPutCounterOnTargetCreature() {
        suspendCard(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters).containsValue(1);
    }

    @Test
    void removingTimeCounterMayBeDeclined() {
        suspendCard(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.exiledCardTimeCounters).containsValue(1);
    }

    private FungalBehemoth suspendCard(int xValue) {
        FungalBehemoth card = new FungalBehemoth();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }
}
