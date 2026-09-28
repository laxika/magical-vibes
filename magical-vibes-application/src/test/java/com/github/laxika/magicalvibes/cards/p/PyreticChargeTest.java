package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreticCharge.class, Forest.class, GrizzlyBears.class})
class PyreticChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the remaining hand, draws four, and boosts own creatures per discard")
    void discardsDrawsAndBoosts() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(
                new PyreticCharge(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws four and gives no boost when no cards remain to discard")
    void emptyHandStillDrawsFourWithoutBoost() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PyreticCharge()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PyreticCharge(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
