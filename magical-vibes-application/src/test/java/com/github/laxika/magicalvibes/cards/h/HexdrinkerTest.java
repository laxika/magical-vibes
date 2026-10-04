package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hexdrinker.class, Shock.class, GrizzlyBears.class})
class HexdrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Hexdrinker's stats and protection at each threshold")
    void levelsUpAtThresholds() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());

        assertStats(hexdrinker, 2, 1);

        prepareForLeveling(player1, 8);
        levelUp(player1, 3);

        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(hexdrinker, 4, 4);

        levelUp(player1, 5);

        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isEqualTo(8);
        assertStats(hexdrinker, 6, 6);
    }

    @Test
    @DisplayName("At level 3, Hexdrinker has protection from instants")
    void levelThreeHasProtectionFromInstants() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 3);
        levelUp(player1, 3);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hexdrinker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from instants");
    }

    @Test
    @DisplayName("At level 8, Hexdrinker cannot be blocked")
    void levelEightHasProtectionFromEverything() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 8);
        levelUp(player1, 8);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hexdrinker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hexdrinker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void levelUpRequiresAnEmptyStack() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    void levelUpCannotBeActivatedDuringCombat() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void levelUpCannotBeActivatedOnAnOpponentsTurn() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 1);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void levelEightProtectionAlsoStopsItsControllersSpells() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 8);
        levelUp(player1, 8);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, hexdrinker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void levelTwoCanStillBeTargetedByInstants() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 2);
        levelUp(player1, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, hexdrinker.getId());

        harness.assertInGraveyard(player1, "Hexdrinker");
    }

    @Test
    void levelSevenCanStillBeBlockedByCreatures() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 7);
        levelUp(player1, 7);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hexdrinker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hexdrinker);
    }

    @Test
    void levelEightPreventsCombatDamageWhileBlocking() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 8);
        levelUp(player1, 8);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(hexdrinker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Hexdrinker");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void prepareForLeveling(Player player, int mana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, mana);
    }

    private void levelUp(Player player, int times) {
        for (int i = 0; i < times; i++) {
            harness.activateAbility(player, 0, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
