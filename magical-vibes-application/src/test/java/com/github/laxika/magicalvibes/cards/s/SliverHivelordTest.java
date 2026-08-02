package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SliverHivelordTest extends BaseCardTest {

    @Test
    @DisplayName("Sliver Hivelord grants itself indestructible (it is a Sliver)")
    void grantsSelfIndestructible() {
        Permanent hivelord = addCreatureReady(player1, new SliverHivelord());

        assertThat(gqs.hasKeyword(gd, hivelord, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Grants indestructible to another Sliver you control")
    void grantsIndestructibleToOtherSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant indestructible to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A granted Sliver survives a destroy spell")
    void grantedSliverSurvivesDestruction() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, otherSliver.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonescythe Sliver");
    }
}
