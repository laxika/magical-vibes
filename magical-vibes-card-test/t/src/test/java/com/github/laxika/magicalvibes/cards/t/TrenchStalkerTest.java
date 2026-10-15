package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrenchStalker.class, GrizzlyBears.class})
class TrenchStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have deathtouch or lifelink before its controller draws two cards")
    void lacksKeywordsBeforeThreshold() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch and lifelink after its controller draws two cards")
    void gainsKeywordsAtThreshold() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not enable the ability")
    void opponentDrawsDoNotCount() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void oneDrawDoesNotEnableKeywordsButSecondAndThirdDrawsDo() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player1, List.of(new TrenchStalker(), new TrenchStalker(), new TrenchStalker()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void drawsBeforeEnteringBattlefieldCount() {
        harness.setLibrary(player1, List.of(new TrenchStalker(), new TrenchStalker()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        Permanent stalker = harness.enterBattlefieldAndReturn(player1, new TrenchStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void controllerDrawsDuringOpponentsTurnEnableKeywords() {
        harness.forceActivePlayer(player2);
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player1, List.of(new TrenchStalker(), new TrenchStalker()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void losesKeywordsWhenNextTurnBegins() {
        harness.forceActivePlayer(player1);
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player1, List.of(new TrenchStalker(), new TrenchStalker()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void enabledKeywordsKillBlockerAndGainLifeFromCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new TrenchStalker());
        Permanent blocker = addCreatureReady(player2, new TrenchStalker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new TrenchStalker(), new TrenchStalker()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Trench Stalker");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
