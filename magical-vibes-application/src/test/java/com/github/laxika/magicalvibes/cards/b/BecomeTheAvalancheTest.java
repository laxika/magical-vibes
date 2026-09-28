package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BecomeTheAvalanche.class, AirElemental.class, GrizzlyBears.class})
class BecomeTheAvalancheTest extends BaseCardTest {

    @Test
    void drawsForQualifyingCreaturesThenBoostsByFinalHandSize() {
        Permanent qualifyingCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent smallerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BecomeTheAvalanche(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, qualifyingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, qualifyingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, smallerCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, smallerCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, qualifyingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, qualifyingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, smallerCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, smallerCreature)).isEqualTo(2);
    }

    @Test
    void drawsNoCardsWhenNoCreatureHasPowerFourAndStillUsesHandSizeForBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BecomeTheAvalanche(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
