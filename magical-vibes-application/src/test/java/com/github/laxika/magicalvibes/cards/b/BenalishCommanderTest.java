package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishCommander.class})
class BenalishCommanderTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualSoldiersYouControl() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new BenalishCommander());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(1);

        harness.addToBattlefield(player1, new BenalishCommander());
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);

        harness.addToBattlefield(player2, new BenalishCommander());
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);
    }

    @Test
    void suspendUsesChosenXAsTimeCounters() {
        BenalishCommander card = suspendCard(3);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void suspendXCannotBeZero() {
        BenalishCommander card = new BenalishCommander();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void removingTimeCounterCreatesSoldierToken() {
        BenalishCommander card = suspendCard(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(soldierTokenCount()).isEqualTo(1);
    }

    @Test
    void lastTimeCounterCreatesSoldierTokenAndOffersSuspendCast() {
        suspendCard(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(soldierTokenCount()).isEqualTo(1);
    }

    private BenalishCommander suspendCard(int xValue) {
        BenalishCommander card = new BenalishCommander();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }

    private long soldierTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier"))
                .count();
    }
}
