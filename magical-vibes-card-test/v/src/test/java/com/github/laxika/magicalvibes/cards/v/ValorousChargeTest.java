package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SleightOfMind;
import com.github.laxika.magicalvibes.model.ManaColor;
import java.util.List;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightErrant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValorousCharge.class, SleightOfMind.class, KnightErrant.class, GrizzlyBears.class})
class ValorousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures get +2/+0, non-white creatures are unaffected")
    void boostsOnlyWhiteCreatures() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant()); // 2/2 White
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());  // 2/2 Green

        castValorousCharge();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creatures also get +2/+0")
    void boostsAllPlayersWhiteCreatures() {
        Permanent ownWhite = addCreatureReady(player1, new KnightErrant());
        Permanent opponentWhite = addCreatureReady(player2, new KnightErrant());

        castValorousCharge();

        assertThat(gqs.getEffectivePower(gd, ownWhite)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentWhite)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());

        castValorousCharge();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("White creatures entering after resolution are not boosted")
    void doesNotBoostWhiteCreaturesEnteringAfterResolution() {
        castValorousCharge();

        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Valorous Charges stack until end of turn")
    void multipleChargesStack() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());
        Permanent greenCreature = addCreatureReady(player2, new GrizzlyBears());

        castValorousCharge();
        castValorousCharge();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("White creatures entering while the spell is on the stack are boosted")
    void includesWhiteCreaturesEnteringBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ValorousCharge(), "{1}{W}{W}");

        Permanent whiteCreature = addCreatureReady(player2, new KnightErrant());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
    }
    private void castValorousCharge() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ValorousCharge(), "{1}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Changed from white to green on the stack, it gives green creatures +2/+0 instead")
    void textChangedSpellBoostsTheNewColor() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ValorousCharge(), "{1}{W}{W}");
        java.util.UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, spellId);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
    }
}
