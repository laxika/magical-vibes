package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.j.JeweledAmulet;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaheeliFiligreeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 scries, then tapping an artifact draws a card")
    void plusOneScriesAndDrawsWhenArtifactIsTapped() {
        Permanent saheeli = addReadySaheeli(player1, 4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
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
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());
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

        harness.setHand(player1, List.of(new JeweledAmulet()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jeweled Amulet");
    }

    private Permanent addReadySaheeli(Player player, int loyalty) {
        Permanent perm = new Permanent(new SaheeliFiligreeMaster());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
