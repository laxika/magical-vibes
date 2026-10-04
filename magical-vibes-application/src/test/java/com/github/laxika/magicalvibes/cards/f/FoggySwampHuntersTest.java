package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoggySwampHunters.class})
class FoggySwampHuntersTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have lifelink or menace before its controller draws two cards")
    void noKeywordsBeforeTwoDraws() {
        Permanent hunters = addHunters();

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();

        harness.setLibrary(player1, List.of(new FoggySwampHunters()));
        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has lifelink and menace after its controller draws two cards")
    void gainsKeywordsAfterTwoDraws() {
        Permanent hunters = addHunters();
        harness.setLibrary(player1, List.of(new FoggySwampHunters(), new FoggySwampHunters()));

        draw(player1);
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();

        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's draws do not grant the keywords")
    void opponentDrawsDoNotCount() {
        Permanent hunters = addHunters();
        harness.setLibrary(player2, List.of(new FoggySwampHunters(), new FoggySwampHunters()));

        draw(player2);
        draw(player2);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();
    }

    @Test
    void countsDrawsBeforeEnteringAndKeepsKeywordsAfterThirdDraw() {
        harness.setLibrary(player1, List.of(new FoggySwampHunters(), new FoggySwampHunters(),
                new FoggySwampHunters()));
        draw(player1);
        draw(player1);

        Permanent hunters = addHunters();

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();

        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();
    }

    @Test
    void losesKeywordsOnNextTurnAndCanRegainThemDuringOpponentsTurn() {
        Permanent hunters = addHunters();
        harness.setLibrary(player1, List.of(new FoggySwampHunters(), new FoggySwampHunters(),
                new FoggySwampHunters(), new FoggySwampHunters()));
        draw(player1);
        draw(player1);
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();

        draw(player1);
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();
        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();
    }

    @Test
    void lifelinkGainsLifeFromCombatDamageAfterTwoDraws() {
        Permanent hunters = addHunters();
        harness.setLibrary(player1, List.of(new FoggySwampHunters(), new FoggySwampHunters()));
        draw(player1);
        draw(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        hunters.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void combatDamageDoesNotGainLifeBeforeTwoDraws() {
        Permanent hunters = addHunters();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        hunters.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void menaceRejectsOneBlockerAndAllowsTwoAfterTwoDraws() {
        Permanent hunters = addHunters();
        hunters.setSummoningSick(false);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new FoggySwampHunters());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new FoggySwampHunters());
        harness.setLibrary(player1, List.of(new FoggySwampHunters(), new FoggySwampHunters()));
        draw(player1);
        draw(player1);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBlocker, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondBlocker, Keyword.MENACE)).isFalse();
    }

    @Test
    void canBeBlockedByOneCreatureBeforeTwoDraws() {
        Permanent hunters = addHunters();
        hunters.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FoggySwampHunters());
        harness.setLibrary(player1, List.of(new FoggySwampHunters()));
        draw(player1);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addHunters() {
        return harness.addToBattlefieldAndReturn(player1, new FoggySwampHunters());
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
