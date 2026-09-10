package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasAvenger.class, FountainOfYouth.class, GrizzlyBears.class})
class GaeasAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness are one plus the number of artifacts opponents control")
    void powerAndToughnessCountOpponentsArtifacts() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GaeasAvenger());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update as opponents gain or lose artifacts")
    void powerAndToughnessUpdateWhenOpponentArtifactsChange() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GaeasAvenger());

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(1);

        harness.addToBattlefield(player2, new FountainOfYouth());
        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(1);
    }
}
