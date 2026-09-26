package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastOfBurden.class, GrizzlyBears.class, HowlingMine.class})
class BeastOfBurdenTest extends BaseCardTest {

    @Test
    @DisplayName("Beast of Burden is 1/1 when it is the only creature on the battlefield")
    void isOneOneWhenOnlyCreature() {
        Permanent beast = addCreatureReady(player1, new BeastOfBurden());

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Beast of Burden counts creatures controlled by any player")
    void countsCreaturesOfAllPlayers() {
        Permanent beast = addCreatureReady(player1, new BeastOfBurden());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Beast + two Grizzly Bears = 3 creatures on the battlefield.
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Beast of Burden power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent beast = addCreatureReady(player1, new BeastOfBurden());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }

    @Test
    @DisplayName("Beast of Burden does not count noncreature permanents")
    void doesNotCountNoncreaturePermanents() {
        Permanent beast = addCreatureReady(player1, new BeastOfBurden());
        harness.addToBattlefield(player2, new HowlingMine());

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(1);
    }
}
