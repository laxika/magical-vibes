package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolitaryCamelTest extends BaseCardTest {

    // ===== Conditional lifelink =====

    @Test
    @DisplayName("Solitary Camel has lifelink while you control a Desert")
    void hasLifelinkWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Solitary Camel has lifelink while a Desert card is in your graveyard")
    void hasLifelinkWithDesertInGraveyard() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Solitary Camel has no lifelink without any Desert")
    void noLifelinkWithoutDesert() {
        harness.addToBattlefield(player1, new SolitaryCamel());

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Desert controlled by the opponent does not grant lifelink")
    void opponentDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new SunscorchedDesert()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Desert card in the opponent's graveyard does not grant lifelink")
    void opponentGraveyardDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.setGraveyard(player2, List.of(new SunscorchedDesert()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Solitary Camel loses lifelink when its only Desert leaves the battlefield")
    void losesLifelinkWhenDesertLeaves() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));

        Permanent camel = findPermanent(player1, "Solitary Camel");
        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sunscorched Desert"));

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A non-Desert land does not grant lifelink")
    void nonDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }
}
