package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AerithRescueMission;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CidTimelessArtificer.class, AerithRescueMission.class, GrizzlyBears.class, Ornithopter.class,
        LiquimetalCoating.class})
class CidTimelessArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Cid boosts artifact creatures and Heroes by Artificers on the battlefield and in the graveyard")
    void boostsArtifactCreaturesAndHeroes() {
        harness.addToBattlefield(player1, new CidTimelessArtificer());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CidTimelessArtificer()));

        harness.setHand(player1, List.of(new AerithRescueMission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        Permanent hero = findPermanents(player1, "Hero").getFirst();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling Cid draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CidTimelessArtificer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cid, Timeless Artificer");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only your Artificers and graveyard cards count, and only your creatures benefit")
    void boostUsesOnlyControllerZones() {
        Permanent cid = harness.addToBattlefieldAndReturn(player1, new CidTimelessArtificer());
        int unboostedCidPower = gqs.getEffectivePower(gd, cid);
        int unboostedCidToughness = gqs.getEffectiveToughness(gd, cid);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new CidTimelessArtificer());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new CidTimelessArtificer(), new CidTimelessArtificer()));

        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingArtifact)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, cid)).isEqualTo(unboostedCidPower);
        assertThat(gqs.getEffectiveToughness(gd, cid)).isEqualTo(unboostedCidToughness);

        harness.setGraveyard(player1, List.of(new CidTimelessArtificer(), new CidTimelessArtificer()));
        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(5);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cid benefits from its own boost when it becomes an artifact creature")
    void boostsItselfWhenItBecomesAnArtifact() {
        Permanent cid = harness.addToBattlefieldAndReturn(player1, new CidTimelessArtificer());
        harness.addToBattlefield(player1, new LiquimetalCoating());

        harness.activateAbility(player1, 1, null, cid.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cid)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cycling increases the boost when Cid is discarded, before the draw resolves")
    void cyclingImmediatelyIncreasesBoost() {
        harness.addToBattlefield(player1, new CidTimelessArtificer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new CidTimelessArtificer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Cid, Timeless Artificer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }
}
