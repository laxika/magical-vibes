package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetalFatigue.class, DarksteelIngot.class, DarksteelGargoyle.class, AuriokGlaivemaster.class})
class MetalFatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps every artifact on every battlefield")
    void tapsAllArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());

        castAndResolveMetalFatigue();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap non-artifact permanents")
    void doesNotTapNonArtifacts() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AuriokGlaivemaster());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());

        castAndResolveMetalFatigue();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolves without any artifacts or targets")
    void resolvesOnEmptyBattlefield() {
        castAndResolveMetalFatigue();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Metal Fatigue");
    }

    @Test
    @DisplayName("Leaves tapped artifacts tapped while tapping the remaining artifacts")
    void handlesAlreadyTappedArtifacts() {
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        tappedArtifact.tap();
        Permanent untappedArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());

        castAndResolveMetalFatigue();

        assertThat(tappedArtifact.isTapped()).isTrue();
        assertThat(untappedArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps artifacts that entered after the spell was cast")
    void determinesArtifactsAtResolution() {
        harness.castFromHand(player1, new MetalFatigue(), "{2}{W}");
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        assertThat(artifact.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Metal Fatigue");
    }

    private void castAndResolveMetalFatigue() {
        harness.castFromHand(player1, new MetalFatigue(), "{2}{W}");
        harness.passBothPriorities();
    }
}
