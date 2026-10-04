package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GloriousDecay.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class})
class GloriousDecayTest extends BaseCardTest {

    private void addGeneric() {
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    @CardUsed({GloriousDecay.class, FountainOfYouth.class, GrizzlyBears.class})
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        }

        @Test
        @DisplayName("Cannot target a non-artifact")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: 4 damage to target creature with flying")
    @CardUsed({GloriousDecay.class, AirElemental.class, GrizzlyBears.class})
    class DamageFlyingMode {

        @Test
        @DisplayName("Deals 4 damage to a flying creature")
        void damagesFlyingCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Air Elemental");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetNonFlyer() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new AirElemental());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Exile target card from a graveyard, draw a card")
    @CardUsed({GloriousDecay.class, GrizzlyBears.class, FountainOfYouth.class})
    class ExileGraveyardMode {

        @Test
        @DisplayName("Exiles a noncreature card from your own graveyard and draws only for you")
        void exilesOwnNoncreatureCard() {
            Card artifact = new FountainOfYouth();
            harness.setGraveyard(player1, List.of(artifact));
            harness.setHand(player1, List.of(new GloriousDecay()));
            harness.setHand(player2, List.of());
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            addGeneric();

            harness.castInstant(player1, 0, 2, artifact.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Fountain of Youth");
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
            harness.assertInHand(player1, "Grizzly Bears");
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Does not draw when the graveyard target leaves before resolution")
        void doesNotDrawWithIllegalTarget() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            harness.setHand(player1, List.of(new GloriousDecay()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            addGeneric();

            harness.castInstant(player1, 0, 2, bears.getId());
            harness.setGraveyard(player2, List.of());
            harness.setExile(player2, List.of(bears));
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Glorious Decay");
        }

        @Test
        @DisplayName("Cannot choose the graveyard mode without a target")
        void cannotCastGraveyardModeWithoutTarget() {
            harness.setGraveyard(player1, List.of());
            harness.setGraveyard(player2, List.of());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, (UUID) null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Exiles a graveyard card and draws a card")
        void exilesAndDraws() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            harness.setHand(player1, List.of(new GloriousDecay()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            addGeneric();

            harness.castInstant(player1, 0, 2, bears.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player2, "Grizzly Bears");
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }
}
