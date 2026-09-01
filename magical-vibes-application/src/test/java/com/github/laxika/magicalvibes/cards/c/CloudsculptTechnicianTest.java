package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudsculptTechnician.class, LeoninScimitar.class, Island.class})
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
}
