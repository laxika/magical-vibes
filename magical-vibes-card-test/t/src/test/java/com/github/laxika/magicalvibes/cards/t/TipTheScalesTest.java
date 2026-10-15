package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TipTheScales.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class TipTheScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and gives all creatures -X/-X based on its toughness")
    void sacrificesCreatureAndWeakensAllCreatures() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTipTheScales();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -X/-X effect wears off at the end of the turn")
    void debuffExpiresAtEndOfTurn() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTipTheScales();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing when there is no creature to sacrifice")
    void doesNothingWithoutCreatureToSacrifice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTipTheScales();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Players can respond after sacrifice before the reduction resolves")
    void sacrificeCreatesSeparateResponseWindow() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTipTheScales();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tip the Scales");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);

        Permanent enteringBeforeResolution = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enteringBeforeResolution)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses modified toughness before sacrifice rather than printed toughness or power")
    void usesLastKnownEffectiveToughness() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrificed.setToughnessModifier(1);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTipTheScales();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("Creatures on both sides die when their toughness becomes zero")
    void reductionKillsCreaturesOnBothSides() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castTipTheScales();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves are not weakened")
    void laterCreaturesAreUnaffected() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTipTheScales();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);
    }

    private void castTipTheScales() {
        harness.setHand(player1, List.<Card>of(new TipTheScales()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
