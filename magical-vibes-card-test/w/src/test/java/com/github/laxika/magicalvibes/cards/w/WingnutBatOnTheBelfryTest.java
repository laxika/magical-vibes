package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FrogButler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingnutBatOnTheBelfry.class, FrogButler.class})
class WingnutBatOnTheBelfryTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering lets Wingnut gain flying")
    void anotherCreatureEnteringGrantsFlying() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());

        castFrogButler();
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Alliance can grant menace or haste")
    void allianceCanGrantMenaceOrHaste() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());

        castFrogButler();
        resolveAllTriggers();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Wingnut does not trigger from its own entry")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new WingnutBatOnTheBelfry(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When Wingnut attacks, each other attacking creature gets +1/+0")
    void otherAttackingCreaturesGetBoosted() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());
        Permanent otherAttacker = addCreatureReady(player1, new FrogButler());
        Permanent stayHome = addCreatureReady(player1, new FrogButler());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(wingnut.getPowerModifier()).isZero();
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(stayHome.getPowerModifier()).isZero();
    }

    @Test
    void allianceCanGrantHasteToSummoningSickWingnut() {
        harness.castFromHand(player1, new WingnutBatOnTheBelfry(), "{1}{R}");
        harness.passBothPriorities();
        Permanent wingnut = findPermanent(player1, "Wingnut, Bat on the Belfry");

        castFrogButler();
        resolveAllTriggers();
        harness.handleListChoice(player1, "HASTE");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.HASTE)).isTrue();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(wingnut.isTapped()).isTrue();
    }

    @Test
    void separateAllianceTriggersCanGrantDifferentKeywords() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());

        castFrogButler();
        resolveAllTriggers();
        harness.handleListChoice(player1, "FLYING");

        castFrogButler();
        resolveAllTriggers();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.HASTE)).isFalse();
    }

    @Test
    void opposingCreatureEnteringDoesNotTriggerAlliance() {
        Permanent wingnut = addCreatureReady(player1, new WingnutBatOnTheBelfry());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new FrogButler(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, wingnut, Keyword.HASTE)).isFalse();
    }

    @Test
    void attackBoostExpiresAtEndOfTurn() {
        addCreatureReady(player1, new WingnutBatOnTheBelfry());
        Permanent otherAttacker = addCreatureReady(player1, new FrogButler());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherAttacker.getPowerModifier()).isZero();
    }

    private void castFrogButler() {
        harness.castFromHand(player1, new FrogButler(), "{1}{G}");
    }
}
