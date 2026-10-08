package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.f.FrogtosserBanneret;
import com.github.laxika.magicalvibes.cards.s.SharedAnimosity;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ChameleonColossus.class, PricklyBoggart.class, SharedAnimosity.class, FrogtosserBanneret.class})
class ChameleonColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability once doubles power and toughness (X = current power)")
    void activatingOnceDoublesStats() {
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Base 4/4, X = 4 → +4/+4 → 8/8.
        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(8);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, doubling again to 16/16")
    void activatingTwiceCompounds() {
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After first: 8/8; second X = 8 → +8/+8 → 16/16.
        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(16);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Stacked activations each use the power when they resolve")
    void stackedActivationsUseResolutionPower() {
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(16);
    }

    @Test
    @DisplayName("Both boosts use power even when toughness differs")
    void boostsBothStatsByPowerRatherThanToughness() {
        harness.addToBattlefield(player1, new SharedAnimosity());
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(4);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(9);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking Chameleon Colossus")
    void blackCreatureCannotBlock() {
        addCreatureReady(player1, new ChameleonColossus());
        addCreatureReady(player2, new PricklyBoggart());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Changeling lets Chameleon Colossus share a creature type with a Goblin attacker")
    void changelingSharesCreatureType() {
        harness.addToBattlefield(player1, new SharedAnimosity());
        Permanent colossus = addCreatureReady(player1, new ChameleonColossus());
        Permanent boggart = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, boggart)).isEqualTo(2);
    }

    @Test
    @DisplayName("Protection prevents black combat damage while allowing Colossus to block")
    void protectionPreventsBlackCombatDamage() {
        addCreatureReady(player1, new FrogtosserBanneret());
        Permanent colossus = addCreatureReady(player2, new ChameleonColossus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(colossus);
        assertThat(colossus.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
