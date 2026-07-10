package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeastOfBurdenTest extends BaseCardTest {

    @Test
    @DisplayName("Beast of Burden is 1/1 when it is the only creature on the battlefield")
    void isOneOneWhenOnlyCreature() {
        Permanent beast = addBeastReady(player1);

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Beast of Burden counts creatures controlled by any player")
    void countsCreaturesOfAllPlayers() {
        Permanent beast = addBeastReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Beast + two Grizzly Bears = 3 creatures on the battlefield.
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Beast of Burden power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent beast = addBeastReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    private Permanent addBeastReady(Player player) {
        BeastOfBurden card = new BeastOfBurden();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
