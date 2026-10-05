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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveTheBoost() {
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PyreticCharge(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    void plotPaysFourManaAndAllowsFreeCastingOnALaterTurn() {
        PyreticCharge charge = new PyreticCharge();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Forest discardedCard = new Forest();
        harness.setHand(player1, List.of(charge, discardedCard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(charge);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThatThrownBy(() -> harness.castFromExile(player1, charge.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, charge.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, charge.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        int discardCount = gd.playerHands.get(player1.getId()).size();

        harness.castFromExile(player1, charge.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard, charge);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(charge);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2 + discardCount);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void plotRequiresFullCostAndSorceryTiming() {
        PyreticCharge charge = new PyreticCharge();
        harness.setHand(player1, List.of(charge));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(charge);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(charge);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
