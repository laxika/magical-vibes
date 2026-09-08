package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatebreakerRamTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Gate controlled")
    void getsBoostForEachControlledGate() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, createGate());
        harness.addToBattlefield(player1, createGate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(4);
    }

    @Test
    @DisplayName("Has vigilance and trample with two controlled Gates")
    void hasKeywordsWithTwoControlledGates() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, createGate());
        harness.addToBattlefield(player1, createGate());

        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not have vigilance or trample with fewer than two controlled Gates")
    void noKeywordsWithOneControlledGate() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, createGate());

        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Gates do not affect the Ram")
    void opponentGatesDoNotCount() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player2, createGate());
        harness.addToBattlefield(player2, createGate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and keywords update when a Gate leaves")
    void updatesWhenGateLeaves() {
        Permanent ram = harness.addToBattlefieldAndReturn(player1, new GatebreakerRam());
        harness.addToBattlefield(player1, createGate());
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, createGate());

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondGate);

        assertThat(gqs.getEffectivePower(gd, ram)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ram)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ram, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.TRAMPLE)).isFalse();
    }

    private Card createGate() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.GATE));
        return card;
    }
}
