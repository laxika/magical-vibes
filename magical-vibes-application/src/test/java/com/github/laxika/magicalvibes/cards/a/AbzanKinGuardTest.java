package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbzanKinGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink while controller controls a white permanent")
    void hasLifelinkWithWhitePermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, coloredCreature(CardColor.WHITE));

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Has lifelink while controller controls a black permanent")
    void hasLifelinkWithBlackPermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, coloredCreature(CardColor.BLACK));

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not have lifelink without a white or black permanent")
    void noLifelinkWithoutMatchingPermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, coloredCreature(CardColor.GREEN));

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's white or black permanent does not grant lifelink")
    void opponentMatchingPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player2, coloredCreature(CardColor.WHITE));

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses lifelink when the matching permanent leaves the battlefield")
    void losesLifelinkWhenMatchingPermanentLeaves() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        Permanent matchingPermanent = harness.addToBattlefieldAndReturn(player1, coloredCreature(CardColor.BLACK));
        Permanent kinGuard = findPermanent(player1, "Abzan Kin-Guard");

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(matchingPermanent);

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isFalse();
    }

    private Card coloredCreature(CardColor color) {
        Card card = new GrizzlyBears();
        card.setColors(List.of(color));
        return card;
    }
}
