package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelJuggernaut;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeedsOfInnocenceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every artifact and leaves non-artifacts alone")
    void destroysOnlyArtifacts() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Millstone"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Each artifact's controller gains life equal to that artifact's mana value")
    void eachControllerGainsManaValueLife() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new IcyManipulator());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 + 4);
    }

    @Test
    @DisplayName("Life gain stacks per artifact, and a zero-cost artifact grants none")
    void lifeGainIsPerArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new Ornithopter());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 + 4);
    }

    @Test
    @DisplayName("An indestructible artifact survives and grants its controller no life")
    void indestructibleArtifactSurvivesAndGrantsNoLife() {
        harness.addToBattlefield(player2, new DarksteelJuggernaut());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Darksteel Juggernaut");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
    }

    @Test
    @DisplayName("No artifacts on the battlefield means no life gain")
    void noArtifactsNoLifeGain() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castSeedsOfInnocence();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
    }

    private void castSeedsOfInnocence() {
        harness.setHand(player1, List.of(new SeedsOfInnocence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
