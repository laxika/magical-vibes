package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhinoTerribleTrampler.class, Forest.class, GrizzlyBears.class, WeldingJar.class})
class RhinoTerribleTramplerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an artifact and distributes counters and trample to other creatures")
    void entersWithAllEtbEffects() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WeldingJar());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        harness.enterBattlefieldAndReturn(player1, new RhinoTerribleTrampler());

        PendingInteraction.PermanentChoice destructionChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(destructionChoice.validIds()).containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.validIds()).contains(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Welding Jar");
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB target groups reject the Rhino itself and noncreatures")
    void targetGroupsHaveCorrectRestrictions() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        Permanent validCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent rhino = harness.enterBattlefieldAndReturn(player1, new RhinoTerribleTrampler());

        PendingInteraction.PermanentChoice destructionChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(destructionChoice.validIds()).containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.validIds()).contains(validCreature.getId())
                .doesNotContain(noncreature.getId(), rhino.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }
}
