package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanDruid.class, CastDown.class})
class KrosanDruidTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Cast without kicker — enters without a life-gain trigger")
    void castWithoutKickerNoLifeGain() {
        harness.castFromHand(player1, new KrosanDruid(), "{2}{G}");
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Krosan Druid");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        castKicked();

        harness.assertOnBattlefield(player1, "Krosan Druid");
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

    @Test
    @DisplayName("Kicked life-gain trigger resolves after the Druid is destroyed")
    void gainsLifeAfterDruidIsDestroyed() {
        castKicked();
        harness.setHand(player2, List.of(new CastDown()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Krosan Druid"));

        harness.assertInGraveyard(player1, "Krosan Druid");
        harness.assertNotOnBattlefield(player1, "Krosan Druid");
        harness.assertLife(player1, STARTING_LIFE);
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE + 10);
        harness.assertLife(player2, STARTING_LIFE);
        assertThat(gd.stack).isEmpty();
    }

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
