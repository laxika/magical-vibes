package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MouthOfTheStorm.class, GrizzlyBears.class, Shock.class})
class MouthOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("ETB weakens existing opposing creatures until your next turn")
    void etbWeakensOpposingCreaturesUntilNextTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castMouthOfTheStorm();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        Permanent lateOpposingCreature = addCreatureReady(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lateOpposingCreature)).isEqualTo(2);

        gd.expireFloatingEffectsAtTurnStart(player2.getId());
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(-1);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent mouth = addCreatureReady(player1, new MouthOfTheStorm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mouth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    private void castMouthOfTheStorm() {
        harness.setHand(player1, List.of(new MouthOfTheStorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
