package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.a.ArcLightning;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerrasHymn.class, ArgothianSwine.class, ArcLightning.class})
class SerrasHymnTest extends BaseCardTest {

    @Test
    void upkeepTriggerMayAddVerseCounter() {
        Permanent hymn = addReadyHymn(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hymn.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void upkeepTriggerCanBeDeclined() {
        Permanent hymn = addReadyHymn(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hymn.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    void upkeepTriggerDoesNotHappenOnOpponentsUpkeep() {
        Permanent hymn = addReadyHymn(player1);

        advanceToUpkeep(player2);

        assertThat(hymn.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    void sacrificePreventsDamageDividedAmongTargets() {
        Permanent hymn = addReadyHymn(player1);
        hymn.setCounterCount(CounterType.VERSE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(creature.getId(), 2, player2.getId(), 1));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(hymn);
        harness.passBothPriorities();

        assertThat(creature.getDamagePreventionShield()).isEqualTo(2);
        assertThat(harness.getGameData().playerDamagePreventionShields.getOrDefault(player2.getId(), 0))
                .isEqualTo(1);
    }

    @Test
    void preventionShieldsPreventAssignedDamage() {
        Permanent hymn = addReadyHymn(player1);
        hymn.setCounterCount(CounterType.VERSE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(creature.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeWithNoVerseCountersCanChooseNoTargets() {
        Permanent hymn = addReadyHymn(player1);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(hymn);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    void preventionAssignmentsMustMatchVerseCounters() {
        Permanent hymn = addReadyHymn(player1);
        hymn.setCounterCount(CounterType.VERSE, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHymn(Player owner) {
        Permanent hymn = new Permanent(new SerrasHymn());
        hymn.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(hymn);
        return hymn;
    }
}
