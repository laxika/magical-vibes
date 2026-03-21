package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KrosanDruidTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 2/3, no life gain")
    void castWithoutKickerNoLifeGain() {
        harness.setHand(player1, List.of(new KrosanDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Krosan Druid"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        castKicked();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Krosan Druid"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — gains 10 life")
    void castWithKickerGains10Life() {
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 10);
    }

    @Test
    @DisplayName("Cast with kicker — opponent life unchanged")
    void castWithKickerOpponentLifeUnchanged() {
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
    }

    // ===== Helpers =====

    private void castKicked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KrosanDruid()));
        // Kicker cost: {4}{G}, base cost: {2}{G} — total: {6}{G}{G}
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }
}
