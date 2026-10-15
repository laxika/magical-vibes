package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SejiriMerfolk.class, Plains.class})
class SejiriMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have first strike or lifelink without a Plains")
    void noKeywordsWithoutPlains() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Has first strike and lifelink while its controller controls a Plains")
    void gainsKeywordsWithPlains() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Plains does not grant the keywords")
    void opponentPlainsDoesNotGrantKeywords() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses the keywords when its controller no longer controls a Plains")
    void losesKeywordsWhenPlainsLeaves() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(plains);

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A tapped Plains still grants first strike and lifelink")
    void tappedPlainsGrantsKeywords() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        plains.tap();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Keeps both keywords when one of multiple Plains leaves")
    void keepsKeywordsWhileAnotherPlainsRemains() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstPlains);

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("With a Plains, kills a blocker before retaliation and gains life")
    void firstStrikeAndLifelinkApplyInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SejiriMerfolk());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new SejiriMerfolk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Sejiri Merfolk");
        harness.assertInGraveyard(player2, "Sejiri Merfolk");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Without a Plains, combat damage does not gain life")
    void noLifelinkWithoutPlainsInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SejiriMerfolk());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
