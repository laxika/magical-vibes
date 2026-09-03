package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrystalGolem;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LionsEyeDiamond;
import com.github.laxika.magicalvibes.cards.m.MarbleDiamond;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedsOfInnocence.class, CrystalGolem.class, Forest.class, LionsEyeDiamond.class,
        MarbleDiamond.class, NobleElephant.class})
class SeedsOfInnocenceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every artifact and leaves non-artifacts alone")
    void destroysOnlyArtifacts() {
        Permanent player1Artifact = harness.addToBattlefieldAndReturn(player1, new MarbleDiamond());
        Permanent player2Artifact = harness.addToBattlefieldAndReturn(player2, new CrystalGolem());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NobleElephant());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(player1Artifact);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactlyInAnyOrder(forest, creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Artifact);
    }

    @Test
    @DisplayName("Each artifact's controller gains life equal to that artifact's mana value")
    void eachControllerGainsManaValueLife() {
        harness.addToBattlefield(player1, new MarbleDiamond());
        harness.addToBattlefield(player2, new CrystalGolem());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 + 4);
    }

    @Test
    @DisplayName("Life gain stacks per artifact, and a zero-cost artifact grants none")
    void lifeGainIsPerArtifact() {
        harness.addToBattlefield(player2, new MarbleDiamond());
        harness.addToBattlefield(player2, new MarbleDiamond());
        harness.addToBattlefield(player2, new LionsEyeDiamond());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 + 4);
    }

    @Test
    @DisplayName("An indestructible artifact survives and grants its controller no life")
    void indestructibleArtifactSurvivesAndGrantsNoLife() {
        CrystalGolem indestructibleCard = new CrystalGolem();
        indestructibleCard.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent indestructibleArtifact = harness.addToBattlefieldAndReturn(player2, indestructibleCard);
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(indestructibleArtifact);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
    }

    @Test
    @DisplayName("No artifacts on the battlefield means no life gain")
    void noArtifactsNoLifeGain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NobleElephant());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The no-regeneration clause destroys an artifact with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CrystalGolem());
        artifact.setRegenerationShield(1);
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 + 4);
    }

    private void castSeedsOfInnocence() {
        harness.setHand(player1, List.of(new SeedsOfInnocence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
