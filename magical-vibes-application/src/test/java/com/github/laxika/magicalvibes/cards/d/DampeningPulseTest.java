package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampeningPulse.class, GrizzlyBears.class})
class DampeningPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures get -1/-0")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own creatures are unaffected")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Penalty is removed when Dampening Pulse leaves")
    void penaltyIsRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dampening Pulse"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
