package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodProwler;
import com.github.laxika.magicalvibes.cards.b.BoulderbranchGolem;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaheeliFiligreeMaster.class, BlanchwoodProwler.class, BoulderbranchGolem.class,
        EnergyRefractor.class, EncroachingMycosynth.class})
class SaheeliFiligreeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 scries, then tapping an artifact draws a card")
    void plusOneScriesAndDrawsWhenArtifactIsTapped() {
        Permanent saheeli = addReadySaheeli(player1, 4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new BlanchwoodProwler()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 can be declined without drawing")
    void plusOneCanBeDeclined() {
        Permanent saheeli = addReadySaheeli(player1, 4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new BlanchwoodProwler()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 creates two flying Thopters with haste until end of turn")
    void minusTwoCreatesHastyThopters() {
        Permanent saheeli = addReadySaheeli(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(2);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        for (Permanent thopter : thopters) {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent thopter : thopters) {
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isFalse();
        }
    }

    @Test
    @DisplayName("-4 grants the artifact creature boost and artifact spell cost reduction")
    void ultimateCreatesArtifactEmblem() {
        Permanent saheeli = addReadySaheeli(player1, 4);
        Permanent artifactCreature = addCreatureReady(player1, new BoulderbranchGolem());
        Permanent nonArtifactCreature = addCreatureReady(player1, new BlanchwoodProwler());
        int artifactPowerBefore = gqs.getEffectivePower(gd, artifactCreature);
        int artifactToughnessBefore = gqs.getEffectiveToughness(gd, artifactCreature);
        int nonArtifactPowerBefore = gqs.getEffectivePower(gd, nonArtifactCreature);
        int nonArtifactToughnessBefore = gqs.getEffectiveToughness(gd, nonArtifactCreature);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(artifactPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(artifactToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, nonArtifactCreature)).isEqualTo(nonArtifactPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nonArtifactCreature)).isEqualTo(nonArtifactToughnessBefore);

        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new BlanchwoodProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("+1 draws the new top card after putting the scried card on the bottom")
    void plusOneDrawsAfterScryAndCanTapSummoningSickArtifactCreature() {
        addReadySaheeli(player1, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BoulderbranchGolem());
        artifact.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new EnergyRefractor(), new BlanchwoodProwler()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Blanchwood Prowler");
        harness.assertNotInHand(player1, "Energy Refractor");
    }

    @Test
    @DisplayName("+1 cannot draw by tapping an already tapped or opposing artifact")
    void plusOneDoesNotDrawWithoutAnEligibleArtifact() {
        addReadySaheeli(player1, 3);
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        tappedArtifact.tap();
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new BlanchwoodProwler()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 lets its controller choose which eligible artifact to tap")
    void plusOneTapsOnlyTheChosenArtifact() {
        addReadySaheeli(player1, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new BoulderbranchGolem());
        harness.setLibrary(player1, List.of(new BlanchwoodProwler()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(first.isTapped()).isFalse();
        assertThat(chosen.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The emblem boosts later artifact creatures but not opposing creatures")
    void emblemAppliesAfterSaheeliLeavesAndOnlyToItsController() {
        addReadySaheeli(player1, 4);
        Permanent opponentCreature = addCreatureReady(player2, new BoulderbranchGolem());
        int opponentPower = gqs.getEffectivePower(gd, opponentCreature);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentCreature);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Saheeli, Filigree Master");
        Permanent ownCreature = addCreatureReady(player1, new BoulderbranchGolem());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(opponentPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(opponentToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(opponentToughness);
    }

    @Test
    @CardUsed({EncroachingMycosynth.class})
    @DisplayName("The emblem boosts creatures made artifacts by continuous effects")
    void emblemBoostsCreatureMadeArtifactByMycosynth() {
        addReadySaheeli(player1, 4);
        Permanent creature = addCreatureReady(player1, new BlanchwoodProwler());
        int powerBefore = gqs.getEffectivePower(gd, creature);
        int toughnessBefore = gqs.getEffectiveToughness(gd, creature);
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        assertThat(gqs.isArtifact(gd, creature)).isTrue();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughnessBefore + 1);
    }

    private Permanent addReadySaheeli(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SaheeliFiligreeMaster());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
