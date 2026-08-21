package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseDawn.class, GrizzlyBears.class, Mountain.class, Shock.class})
class FalseDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Turns colored mana from your abilities white and lets you spend it as any color")
    void replacesManaAndAllowsWhiteAsAnyColor() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new FalseDawn(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
