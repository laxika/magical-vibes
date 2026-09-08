package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KillZoneAcrobatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Kill-Zone Acrobat flying")
    void sacrificingAnotherCreatureGrantsFlying() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.hasKeyword(gd, acrobat, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Sacrificing another artifact gives Kill-Zone Acrobat flying")
    void sacrificingAnotherArtifactGrantsFlying() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gqs.hasKeyword(gd, acrobat, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not give Kill-Zone Acrobat flying")
    void decliningSacrificeDoesNothing() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, acrobat, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gqs.hasKeyword(gd, acrobat, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, acrobat, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
    }
}
