package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Whipflare.class, GrizzlyBears.class, PorcelainLegionnaire.class, OgreMenial.class})
class WhipflareTest extends BaseCardTest {

    @Test
    @DisplayName("Whipflare kills nonartifact creatures with toughness 2 or less on both sides")
    void killsNonArtifactCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Whipflare does not damage artifact creatures")
    void doesNotDamageArtifactCreatures() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Porcelain Legionnaire");
    }

    @Test
    @DisplayName("Whipflare damages nonartifact creatures but leaves artifact creatures unharmed")
    void selectivelyDamages() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Artifact creature survives
        harness.assertOnBattlefield(player2, "Porcelain Legionnaire");
        // Nonartifact creature dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Whipflare does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Whipflare marks exactly two damage on surviving nonartifact creatures")
    void marksDamageOnSurvivingCreatures() {
        var creature = harness.addToBattlefieldAndReturn(player2, new OgreMenial());
        var artifact = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ogre Menial");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(artifact.getMarkedDamage()).isZero();
    }
}
