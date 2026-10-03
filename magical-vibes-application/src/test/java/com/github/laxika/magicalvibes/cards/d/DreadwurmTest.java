package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeedTheSwarm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadwurm.class, Forest.class, FeedTheSwarm.class})
class DreadwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when a land you control enters")
    void gainsIndestructibleOnLandfall() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentsLand() {
        Permanent dreadwurm = harness.addToBattlefieldAndReturn(player1, new Dreadwurm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Loses landfall-granted indestructible at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall uses the stack and grants nothing before resolution")
    void indestructibleWaitsForTriggerResolution() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Landfall triggers for a land entering without being played")
    void triggersForLandPutOntoBattlefield() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A Dreadwurm entering after the land does not receive another Dreadwurm's grant")
    void onlyTriggeringDreadwurmGainsIndestructible() {
        Permanent original = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Dreadwurm());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall indestructible prevents destruction through the rest of the turn")
    void survivesDestroyEffectAfterLandfall() {
        Permanent dreadwurm = harness.addToBattlefieldAndReturn(player2, new Dreadwurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FeedTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, dreadwurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dreadwurm");
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent addDreadwurmReady() {
        Permanent dreadwurm = harness.addToBattlefieldAndReturn(player1, new Dreadwurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return dreadwurm;
    }
}
