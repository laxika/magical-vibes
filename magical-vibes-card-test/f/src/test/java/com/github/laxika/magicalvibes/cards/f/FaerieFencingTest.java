package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.l.LuminousRebuke;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieFencing.class, GrizzlyBears.class, FaerieDreamthief.class, LuminousRebuke.class, HamletGlutton.class})
class FaerieFencingTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X without the Faerie bonus")
    void appliesBaseMinusXMinusX() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Applies the Faerie bonus based on the battlefield at cast time")
    void appliesFaerieBonusEvenIfFaerieLeavesBeforeResolution() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new FaerieDreamthief());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.setHand(player2, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 5);

        var faerieId = faerie.getId();
        var bearId = bear.getId();
        harness.castInstant(player1, 0, 1, bearId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, faerieId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Faerie Dreamthief");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X can be zero without a Faerie, including when only the opponent controls one")
    void zeroXDoesNotGetOpponentsFaerieBonus() {
        harness.addToBattlefield(player2, new FaerieDreamthief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HamletGlutton());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Faerie Fencing");
    }

    @Test
    @DisplayName("X zero still gets the Faerie bonus and can target your own creature")
    void zeroXAppliesBonusToOwnCreature() {
        harness.addToBattlefield(player1, new FaerieDreamthief());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HamletGlutton());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Faerie bonus adds to X only once and both reductions expire at end of turn")
    void bonusIsAddedOnceAndExpiresWithBaseReduction() {
        harness.addToBattlefield(player1, new FaerieDreamthief());
        harness.addToBattlefield(player1, new FaerieDreamthief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HamletGlutton());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("A Faerie appearing after casting does not grant the bonus")
    void faerieAppearingAfterCastDoesNotGrantBonus() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HamletGlutton());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.addToBattlefield(player1, new FaerieDreamthief());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
