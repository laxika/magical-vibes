package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianSoulgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep sacrifices a creature and keeps Phyrexian Soulgorger")
    void paysCumulativeUpkeep() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soulgorger).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Phyrexian Soulgorger")
    void declineSacrifices() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soulgorger);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
    }

    @Test
    @DisplayName("Phyrexian Soulgorger can be sacrificed to pay its own cumulative upkeep")
    void canSacrificeItselfToPay() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soulgorger);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
    }

    @Test
    @DisplayName("Second upkeep requires sacrificing two creatures")
    void secondUpkeepSacrificesTwoCreatures() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBears.getId()));

        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstBears);

        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                soulgorger.getId(), secondBears.getId(), thirdBears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(secondBears.getId(), thirdBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soulgorger).doesNotContain(secondBears, thirdBears);
    }
}
