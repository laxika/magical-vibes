package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClashOfTheEikons.class, GrizzlyBears.class, HillGiant.class, HistoryOfBenalia.class})
class ClashOfTheEikonsTest extends BaseCardTest {

    @Test
    @DisplayName("Fight mode makes your creature fight an opponent's creature")
    void fightMode() {
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(ownCreature.getId(), opponentCreature.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Putting a lore counter on a Saga triggers its next chapter")
    void putLoreCounterTriggersChapter() {
        Permanent saga = addSaga(player1, 0);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(saga.getId()), null);
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Removing a lore counter from a Saga does not trigger a chapter")
    void removeLoreCounterDoesNotTriggerChapter() {
        Permanent saga = addSaga(player1, 2);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(saga.getId()), null);
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Lore-counter modes reject a non-Saga target")
    void loreCounterModesRequireSaga() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1}, List.of(creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = new Permanent(new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }
}
