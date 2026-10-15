package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbominationIrradiatedBrute;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StiltManToweringTerror.class, SolRing.class, Forest.class, GrizzlyBears.class,
        AbominationIrradiatedBrute.class})
class StiltManToweringTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("A Villain's combat damage steals a noncreature, nonland permanent and prevents sacrificing it")
    void stealsAndProtectsTargetPermanent() {
        addCreatureReady(player1, new StiltManToweringTerror());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId())
                .doesNotContain(land.getId(), creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isTrue();
    }

    @Test
    void controlAndSacrificeRestrictionExpireAfterNextTurn() {
        addCreatureReady(player1, new StiltManToweringTerror());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isFalse();
    }

    @Test
    void multipleVillainsDealDamageButStealOnlyOnePermanent() {
        addCreatureReady(player1, new StiltManToweringTerror());
        addCreatureReady(player1, new AbominationIrradiatedBrute());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void nonVillainCombatDamageDoesNotTriggerTheft() {
        harness.addToBattlefield(player1, new StiltManToweringTerror());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isFalse();
    }
}
