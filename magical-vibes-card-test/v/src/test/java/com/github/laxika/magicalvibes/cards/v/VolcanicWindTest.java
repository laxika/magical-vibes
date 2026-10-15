package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicWind.class, FreshVolunteers.class, Forest.class})
class VolcanicWindTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new VolcanicWind()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Divides damage equal to all creatures on the battlefield among target creatures")
    void dividesBattlefieldCreatureCountDamage() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        prepare();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts creatures but not other permanents")
    void countsOnlyCreaturesOnBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new Forest());
        prepare();

        harness.castSorcery(player1, 0, Map.of(target.getId(), 1));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .containsExactly(target);
    }

    @Test
    @DisplayName("Resolves without targets when there are no creatures")
    void resolvesWithNoCreatures() {
        prepare();

        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Volcanic Wind");
    }

    @Test
    @DisplayName("Locks the creature count at cast time")
    void locksCreatureCountAtCastTime() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new FreshVolunteers());
        prepare();

        harness.castSorcery(player1, 0, Map.of(target.getId(), 3));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(other);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires assignments to sum to the cast-time creature count")
    void assignmentsMustSumToCreatureCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot assign damage to a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Cannot choose zero targets while creatures are on the battlefield")
    void requiresTargetWhenCreaturesExist() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each target must be assigned at least one damage")
    void rejectsZeroDamageAssignment() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(first.getId(), 2, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(land.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal target's assigned damage is not redistributed")
    void doesNotRedistributeDamageFromRemovedTarget() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        prepare();

        harness.castSorcery(player1, 0, Map.of(removed.getId(), 2, remaining.getId(), 1));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).containsExactly(remaining);
        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Volcanic Wind");
    }

    @Test
    @DisplayName("Creatures entering after casting do not increase assigned damage")
    void enteringCreaturesDoNotIncreaseDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        prepare();

        harness.castSorcery(player1, 0, Map.of(target.getId(), 1));
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
