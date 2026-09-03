package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SkyDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuildersBane.class, SkyDiamond.class, BayFalcon.class})
class BuildersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted artifacts and damages each player for the ones they controlled")
    void damagesPerControllerCount() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SkyDiamond());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 7); // X=3: {3}{3}{R}

        harness.castSorcery(player1, 0, 3, List.of(a1.getId(), a2.getId(), own.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sky Diamond");
        harness.assertInGraveyard(player1, "Sky Diamond");
        harness.assertLife(player2, 18); // two of their artifacts died
        harness.assertLife(player1, 19); // one of the caster's own died
    }

    @Test
    @DisplayName("A player who controlled none of the destroyed artifacts takes no damage")
    void untouchedPlayerTakesNoDamage() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 3); // X=1: {1}{1}{R}

        harness.castSorcery(player1, 0, 1, List.of(a1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Only artifacts actually put into a graveyard this way count for damage")
    void countsOnlyArtifactsActuallyDestroyed() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 5); // X=2

        harness.castSorcery(player1, 0, 2, List.of(a1.getId(), a2.getId()));

        // One targeted artifact leaves the battlefield before resolution.
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(a2.getId()));

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("May destroy fewer artifacts than X")
    void mayDestroyFewerArtifactsThanX() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SkyDiamond());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 5); // X=2: {2}{2}{R}

        harness.castSorcery(player1, 0, 2, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sky Diamond");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("X=0 destroys nothing and deals no damage")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new SkyDiamond());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 1); // X=0: {R}

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sky Diamond");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonArtifact() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        harness.setHand(player1, List.of(new BuildersBane()));
        harness.addMana(player1, ManaColor.RED, 3); // X=1

        UUID falconId = falcon.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(falconId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts");
    }
}
