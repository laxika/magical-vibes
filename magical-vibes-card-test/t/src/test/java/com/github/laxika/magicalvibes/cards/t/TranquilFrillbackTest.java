package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilFrillback.class, FountainOfYouth.class, GrizzlyBears.class, TrainingGrounds.class})
class TranquilFrillbackTest extends BaseCardTest {

    @Test
    void paysUpToThreeTimesAndChoosesThatManyModes() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice paymentChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(paymentChoice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 3);
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        harness.handleListChoice(player1, "Exile target player's graveyard");
        harness.handleListChoice(player1, "You gain 4 life");

        PendingInteraction.PermanentChoice artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(artifactChoice.validIds()).containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.PermanentChoice playerChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerChoice.validIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void decliningPaymentDoesNothing() {
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void payingOnceAllowsChoosingNoModes() {
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, "Done");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void lifeModeIsChosenBeforeTheReflexiveTriggerResolves() {
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, "You gain 4 life");

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
    }

    @Test
    void canExileItsControllersEntireGraveyard() {
        harness.setGraveyard(player1, List.of(new TranquilFrillback(), new TranquilFrillback()));
        harness.setGraveyard(player2, List.of(new TranquilFrillback()));
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, "Exile target player's graveyard");
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void payingThreeTimesAllowsChoosingOnlyTheLifeMode() {
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 3);
        harness.handleListChoice(player1, "You gain 4 life");
        harness.handleListChoice(player1, "Done");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void destroyModeCanTargetAnEnchantmentButNotAnOrdinaryCreature() {
        var enchantment = harness.addToBattlefieldAndReturn(player2, new TrainingGrounds());
        harness.addToBattlefield(player2, new TranquilFrillback());
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        var targetChoice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(enchantment.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.assertOnBattlefield(player2, "Training Grounds");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Training Grounds");
        harness.assertInGraveyard(player2, "Training Grounds");
        harness.assertOnBattlefield(player2, "Tranquil Frillback");
        harness.assertLife(player1, 20);
    }
}
