package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbsoluteLawTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have protection from red")
    void grantsProtectionFromRedToAllCreatures() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasProtectionFrom(gd, ownBears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentBears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownBears, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from red ends when Absolute Law leaves the battlefield")
    void protectionEndsWhenAbsoluteLawLeaves() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Absolute Law"));

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
    }
}
