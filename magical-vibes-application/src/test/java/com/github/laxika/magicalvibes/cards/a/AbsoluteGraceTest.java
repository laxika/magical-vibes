package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbsoluteGraceTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have protection from black")
    void grantsProtectionFromBlackToAllCreatures() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasProtectionFrom(gd, ownBears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentBears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownBears, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from black ends when Absolute Grace leaves the battlefield")
    void protectionEndsWhenAbsoluteGraceLeaves() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Absolute Grace"));

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isFalse();
    }
}
