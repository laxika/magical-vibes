package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Depressurize;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SinisterCryologist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightformHarmonizer.class, Forest.class, SinisterCryologist.class, Depressurize.class})
class MightformHarmonizerTest extends BaseCardTest {

    @Test
    void landfallDoublesThePowerOfTargetCreatureYouControlUntilEndOfTurn() {
        harness.addToBattlefield(player1, new MightformHarmonizer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinisterCryologist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void landfallCanTargetOnlyYourCreature() {
        harness.addToBattlefield(player1, new MightformHarmonizer());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void canCastItForWarpAndItIsExiledAtTheNextEndStep() {
        MightformHarmonizer harmonizer = new MightformHarmonizer();
        harness.setHand(player1, List.of(harmonizer));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mightform Harmonizer");

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Mightform Harmonizer");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mightform Harmonizer");
        assertThat(gd.findExiledCard(harmonizer.getId())).isNotNull();
    }

    @Test
    void landfallDoublesNegativePowerWithoutChangingToughness() {
        Permanent harmonizer = harness.addToBattlefieldAndReturn(player1, new MightformHarmonizer());
        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player2, new SinisterCryologist());
            harness.handlePermanentChosen(player2, harmonizer.getId());
            harness.passBothPriorities();
        }
        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(-2);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, harmonizer.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, harmonizer)).isEqualTo(4);
    }

    @Test
    void landfallUsesPowerAtResolutionRatherThanWhenTheLandEntered() {
        Permanent harmonizer = harness.addToBattlefieldAndReturn(player1, new MightformHarmonizer());
        harness.setHand(player1, List.of(new Forest(), new Depressurize()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, harmonizer.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harmonizer.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harmonizer)).isEqualTo(4);
    }

    @Test
    void multipleLandfallTriggersDoubleTheAlreadyModifiedPower() {
        Permanent harmonizer = harness.addToBattlefieldAndReturn(player1, new MightformHarmonizer());
        harness.addToBattlefield(player1, new MightformHarmonizer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, harmonizer.getId());
        harness.handlePermanentChosen(player1, harmonizer.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(8);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, harmonizer)).isEqualTo(4);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent harmonizer = harness.addToBattlefieldAndReturn(player1, new MightformHarmonizer());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, harmonizer)).isEqualTo(4);
    }

    @Test
    void warpedCardCanBeCastFromExileForItsNormalCostOnALaterTurn() {
        MightformHarmonizer harmonizer = new MightformHarmonizer();
        harness.setHand(player1, List.of(harmonizer));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(harmonizer.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, harmonizer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mightform Harmonizer");
        assertThat(gd.findExiledCard(harmonizer.getId())).isNull();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mightform Harmonizer");
    }

    @Test
    void castingForNormalCostDoesNotExileItAtEndStep() {
        harness.setHand(player1, List.of(new MightformHarmonizer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mightform Harmonizer");
    }
}
