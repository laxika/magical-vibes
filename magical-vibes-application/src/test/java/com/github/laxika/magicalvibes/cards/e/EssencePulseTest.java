package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssencePulse.class, AirElemental.class, ColossalDreadmaw.class, Revitalize.class, GrizzlyBears.class})
class EssencePulseTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life and gives every creature -X/-X based on life gained this turn")
    void gainsLifeAndShrinksAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        Permanent opposingCreature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new EssencePulse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Includes life gained earlier in the turn")
    void includesEarlierLifeGainThisTurn() {
        Permanent ownCreature = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent opposingCreature = addCreatureReady(player2, new ColossalDreadmaw());

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Revitalize(), new EssencePulse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -X/-X effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new EssencePulse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures with zero toughness die on both sides")
    void killsCreaturesWithZeroToughness() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EssencePulse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Life gained after resolution does not increase the reduction")
    void laterLifeGainDoesNotChangeReduction() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new EssencePulse(), new Revitalize()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void laterCreaturesAreUnaffected() {
        harness.setHand(player1, List.of(new EssencePulse(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent life gain does not contribute to X")
    void ignoresOpponentLifeGain() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Revitalize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player1, List.of(new EssencePulse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 23);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
