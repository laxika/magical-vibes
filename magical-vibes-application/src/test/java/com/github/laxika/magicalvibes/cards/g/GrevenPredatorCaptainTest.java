package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrevenPredatorCaptain.class, HillGiant.class, GrizzlyBears.class})
class GrevenPredatorCaptainTest extends BaseCardTest {

    @Test
    void getsPowerForLifeLostThisTurn() {
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());

        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(5);
        gd.lifeLostThisTurn.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, greven)).isEqualTo(5);
    }

    @Test
    void attackingMaySacrificeAnotherCreatureToDrawAndLoseLife() {
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        gd.lifeLostThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hillGiant.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(10);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void mayDeclineToSacrificeAnotherCreature() {
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new GrevenPredatorCaptain());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsAndLosesLifeDuringTheOriginalAttackTriggerResolution() {
        addCreatureReady(player1, new GrevenPredatorCaptain());
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void usesModifiedPowerAndToughnessImmediatelyBeforeSacrifice() {
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        sacrifice.setPowerModifier(1);
        sacrifice.setToughnessModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HillGiant(), new HillGiant(), new HillGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 16);
        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(9);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void lifeGainAndOpponentsLifeLossDoNotChangeTheBonus() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 4, "test"));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 6));
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 7, "test"));
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());

        harness.assertLife(player1, 22);
        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, greven)).isEqualTo(5);
    }

    @Test
    void canOnlySacrificeAnotherCreatureYouControl() {
        addCreatureReady(player1, new GrevenPredatorCaptain());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new HillGiant(), new HillGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(sacrifice.getId());

        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Greven, Predator Captain");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 18);
    }
}
