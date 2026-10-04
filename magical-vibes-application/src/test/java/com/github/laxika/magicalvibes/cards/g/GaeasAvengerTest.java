package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasAvenger.class, FountainOfYouth.class, GrizzlyBears.class, Ornithopter.class})
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

    @Test
    @DisplayName("Artifact creatures count once, while artifacts outside the battlefield do not count")
    void countsArtifactCreaturesOnlyOnTheBattlefield() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GaeasAvenger());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setGraveyard(player2, List.of(new Ornithopter()));
        harness.setExile(player2, List.of(new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("The artifact count follows the Avenger's current controller")
    void artifactCountFollowsCurrentController() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GaeasAvenger());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(avenger);
        gd.playerBattlefields.get(player2.getId()).add(avenger);

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("The defining ability works in hand and in the graveyard")
    void powerAndToughnessAreDefinedOutsideTheBattlefield() {
        GaeasAvenger avenger = new GaeasAvenger();
        harness.setHand(player1, List.of(avenger));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectiveCardPower(gd, avenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, avenger)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(avenger));

        assertThat(gqs.getEffectiveCardPower(gd, avenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, avenger)).isEqualTo(3);
    }
}
