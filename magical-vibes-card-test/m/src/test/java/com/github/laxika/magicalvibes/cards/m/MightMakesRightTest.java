package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MightMakesRight.class, ThunderingGiant.class, RuneclawBear.class,
        TitanicGrowth.class, Naturalize.class, LightningStrike.class})
class MightMakesRightTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Steals, untaps and hastes the target when you control the biggest creature")
    void stealsTargetWhenConditionMet() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.tap();

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Control reverts to the opponent at end of turn")
    void controlRevertsAtEndOfTurn() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Does not trigger while an opponent controls a creature with the greatest power")
    void doesNotTriggerWhenOpponentHasBiggestCreature() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new ThunderingGiant());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature ties for the greatest power")
    void doesNotTriggerOnTie() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        harness.addToBattlefield(player2, new ThunderingGiant());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        harness.addToBattlefield(player2, new RuneclawBear());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNothingIfOpponentGainsGreatestPowerBeforeResolution() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void triggersUsingEffectivePowerAndAllowsMultipleGreatestCreaturesYouControl() {
        harness.addToBattlefield(player1, new MightMakesRight());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new ThunderingGiant());
        harness.setHand(player1, List.of(new TitanicGrowth(), new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, firstBear.getId());
        harness.castAndResolveInstant(player1, 0, secondBear.getId());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Giant");
        harness.assertNotOnBattlefield(player2, "Thundering Giant");
    }

    @Test
    void stillResolvesAfterEnchantmentLeavesBattlefield() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might Makes Right");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void hasNoTargetWhenOpponentControlsNoCreatures() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thundering Giant");
    }

    @Test
    void doesNothingIfYourGreatestCreatureDiesBeforeResolution() {
        harness.addToBattlefield(player1, new MightMakesRight());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thundering Giant");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void doesNotStealAnotherCreatureWhenTargetDiesBeforeResolution() {
        harness.addToBattlefield(player1, new MightMakesRight());
        harness.addToBattlefield(player1, new ThunderingGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        survivor.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void offersOnlyOpponentCreaturesAsTargets() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MightMakesRight());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new ThunderingGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new MightMakesRight());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(bears.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(
                enchantment.getId(), giant.getId(), opposingEnchantment.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }
}
