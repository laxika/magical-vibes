package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SurveyMechan;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudsculptTechnician.class, LeoninScimitar.class, Island.class, SurveyMechan.class})
class CloudsculptTechnicianTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats without a controlled artifact")
    void noControlledArtifact() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 while its controller controls an artifact")
    void controlledArtifactGrantsBoost() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the boost when the controlled artifact leaves")
    void losesBoostWhenArtifactLeaves() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple artifacts grant only one boost, which remains until the last artifact leaves")
    void multipleArtifactsGrantOnlyOneBoost() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new SurveyMechan());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstArtifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstArtifact)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(secondArtifact);

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("The condition follows the technician's current controller")
    void boostFollowsCurrentController() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.addToBattlefield(player1, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(technician);
        gd.playerBattlefields.get(player2.getId()).add(technician);

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);

        harness.addToBattlefield(player2, new SurveyMechan());

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }

    @Test
    @DisplayName("Artifact cards in hand and graveyard do not satisfy the condition")
    void artifactsOutsideBattlefieldDoNotCount() {
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());
        harness.setHand(player1, List.of(new SurveyMechan()));
        harness.setGraveyard(player1, List.of(new SurveyMechan()));

        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(4);
    }
}
