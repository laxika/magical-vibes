package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhorClanWrecker.class, RhythmOfTheWild.class})
class GhorClanWreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Ghor-Clan Wrecker a +1/+1 counter")
    void riotAddsCounter() {
        Permanent wrecker = castWrecker(true);

        assertThat(wrecker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wrecker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wrecker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives Ghor-Clan Wrecker lasting haste")
    void riotAddsPersistentHaste() {
        Permanent wrecker = castWrecker(false);

        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isTrue();
    }

    @Test
    void menaceRejectsOneBlocker() {
        Permanent wrecker = castWrecker(false);
        addCreatureReady(player2, new GhorClanWrecker());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wrecker.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        castWrecker(false);
        addCreatureReady(player2, new GhorClanWrecker());
        addCreatureReady(player2, new GhorClanWrecker());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    void additionalRiotCanGiveTwoCounters() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        Permanent wrecker = castWrecker(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wrecker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isFalse();
    }

    @Test
    void additionalRiotCanGiveCounterAndHaste() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        Permanent wrecker = castWrecker(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wrecker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isTrue();
    }

    private Permanent castWrecker(boolean chooseCounter) {
        harness.setHand(player1, List.of(new GhorClanWrecker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return findPermanent(player1, "Ghor-Clan Wrecker");
    }
}
