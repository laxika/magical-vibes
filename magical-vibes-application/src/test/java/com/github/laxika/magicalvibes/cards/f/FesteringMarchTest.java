package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FesteringMarch.class, BlindPhantasm.class})
class FesteringMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens only creatures opponents control and is exiled with three time counters")
    void weakensOpponentsAndIsExiledWithSuspendCounters() {
        Permanent ownCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent enemyCreature = addCreatureReady(player2, new BlindPhantasm());
        FesteringMarch march = new FesteringMarch();
        harness.castFromHand(player1, march, "{3}{B}{B}");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyCreature)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(march);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(march.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void weakensUntilEndOfTurn() {
        Permanent enemyCreature = addCreatureReady(player2, new BlindPhantasm());
        FesteringMarch march = new FesteringMarch();
        harness.castFromHand(player1, march, "{3}{B}{B}");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Suspend casts Festering March for free and exiles it again with three time counters")
    void suspendCastsForFree() {
        Permanent enemyCreature = addCreatureReady(player2, new BlindPhantasm());
        FesteringMarch march = suspendMarch();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(march);
        assertThat(gd.exiledCardTimeCounters).containsEntry(march.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyCreature)).isEqualTo(2);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(march.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the free suspend cast leaves Festering March exiled without time counters")
    void canDeclineSuspendCast() {
        Permanent enemyCreature = addCreatureReady(player2, new BlindPhantasm());
        FesteringMarch march = suspendMarch();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyCreature)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(march);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(march.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    private FesteringMarch suspendMarch() {
        FesteringMarch march = new FesteringMarch();
        harness.setHand(player1, List.of(march));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, null);
        return march;
    }
}
