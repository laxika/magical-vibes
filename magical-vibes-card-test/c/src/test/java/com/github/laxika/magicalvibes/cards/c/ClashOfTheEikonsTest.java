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

    @Test
    @DisplayName("All modes resolve in printed order and restoring lore retriggers the chapter")
    void allModesOnSameSaga() {
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent saga = addSaga(player1, 2);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(ownCreature.getId(), opponentCreature.getId(), saga.getId(), saga.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, saga);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .singleElement().satisfies(entry -> assertThat(entry.getDescription()).contains("chapter II"));
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
    }

    @Test
    @DisplayName("The lore modes can target different Sagas")
    void loreModesUseTheirOwnTargets() {
        Permanent removeTarget = addSaga(player1, 2);
        Permanent putTarget = addSaga(player1, 1);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(removeTarget.getId(), putTarget.getId()), null);
        harness.passBothPriorities();

        assertThat(removeTarget.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(putTarget.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .singleElement().satisfies(entry -> assertThat(entry.getSourcePermanentId()).isEqualTo(putTarget.getId()));
    }

    @Test
    @DisplayName("Removing lore from a Saga with no lore counters is legal and does nothing")
    void removeLoreFromZero() {
        Permanent saga = addSaga(player1, 0);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(saga.getId()), null);
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Both lore-counter modes reject an opponent's Saga")
    void loreModesRequireYourSaga() {
        Permanent saga = addSaga(player2, 1);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1}, List.of(saga.getId()), null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{2}, List.of(saga.getId()), null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A missing fight target prevents the fight but leaves the lore mode effective")
    void missingFightTargetDoesNotStopLoreMode() {
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent saga = addSaga(player1, 1);
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(ownCreature.getId(), opponentCreature.getId(), saga.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature.getCard());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getSourcePermanentId().equals(saga.getId()));
    }

    @Test
    @DisplayName("Fight damage is simultaneous so both equally sized creatures die")
    void bothFightersDie() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClashOfTheEikons()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(ownCreature.getId(), opponentCreature.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }
}
