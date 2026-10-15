package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.p.Pilfer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TributeToUrborg.class, AirElemental.class, FountainOfYouth.class,
        GiantGrowth.class, GrizzlyBears.class, Shock.class, Impulse.class, Pilfer.class,
        TolarianTerror.class})
class TributeToUrborgTest extends BaseCardTest {

    @Test
    void givesTargetCreatureMinusTwoMinusTwoWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(false, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void kickedPenaltyScalesWithInstantAndSorceryCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Card artifact = new FountainOfYouth();
        harness.addToBattlefield(player2, artifact);
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid target");
    }

    @Test
    void kickedSpellCountsBothCardTypesButNotCreaturesOrOpponentsGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TolarianTerror());
        harness.setGraveyard(player1, List.of(new Impulse(), new Pilfer(), new TolarianTerror()));
        harness.setGraveyard(player2, List.of(new Impulse(), new Pilfer()));

        cast(true, target.getId());

        harness.assertOnBattlefield(player1, "Tolarian Terror");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void kickedSpellWithEmptyGraveyardDoesNotCountItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TolarianTerror());
        harness.setGraveyard(player1, List.of());

        cast(true, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.assertInGraveyard(player1, "Tribute to Urborg");
    }

    @Test
    void unkickedSpellIgnoresInstantAndSorceryCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TolarianTerror());
        harness.setGraveyard(player1, List.of(new Impulse(), new Pilfer()));

        cast(false, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void kickedSpellCountsGraveyardAtResolutionAndThenFixesThePenalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TolarianTerror());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedInstant(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of(new Impulse(), new Pilfer()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void bothKickedPenaltiesExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TolarianTerror());
        harness.setGraveyard(player1, List.of(new Impulse(), new Pilfer()));
        cast(true, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void basePenaltyKillsCreatureWithTwoToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        cast(false, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void cast(boolean kicked, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castKickedInstant(player1, 0, targetId);
            harness.passBothPriorities();
        } else {
            harness.castAndResolveInstant(player1, 0, targetId);
        }
    }
}
