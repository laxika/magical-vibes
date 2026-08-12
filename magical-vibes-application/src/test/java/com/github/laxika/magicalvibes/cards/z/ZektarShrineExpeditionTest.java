package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Zektar Shrine Expedition")
class ZektarShrineExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = addExpedition();
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
    void decliningLandfallAddsNoQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing creates a hasty trampling Elemental")
    void removesCountersSacrificesAndCreatesElemental() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
        assertThat(elemental.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Elemental is exiled at the beginning of the next end step")
    void elementalIsExiledAtNextEndStep() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without three quest counters")
    void cannotActivateWithoutThreeQuestCounters() {
        addExpedition();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addExpedition() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new ZektarShrineExpedition());
        expedition.setSummoningSick(false);
        return expedition;
    }
}
