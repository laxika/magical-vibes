package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FesteringMarch.class, GrizzlyBears.class})
class FesteringMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens only creatures opponents control and is exiled with three time counters")
    void weakensOpponentsAndIsExiledWithSuspendCounters() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        FesteringMarch march = new FesteringMarch();
        harness.setHand(player1, List.of(march));
        addMarchMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(march);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(march.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void weakensUntilEndOfTurn() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FesteringMarch()));
        addMarchMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Suspend casts Festering March for free and exiles it again with three time counters")
    void suspendCastsForFree() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        FesteringMarch march = new FesteringMarch();
        harness.setHand(player1, List.of(march));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(march.getId(), player1.getId(), 3));
    }

    private void addMarchMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
