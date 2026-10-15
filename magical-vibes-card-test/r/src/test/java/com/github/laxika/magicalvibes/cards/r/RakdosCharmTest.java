package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DaggerdromeImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
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

@CardUsed({RakdosCharm.class, GrizzlyBears.class, HillGiant.class, Millstone.class, DaggerdromeImp.class})
class RakdosCharmTest extends BaseCardTest {

    private void addBR() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Nested
    @DisplayName("Mode 0: Exile target player's graveyard")
    @CardUsed({RakdosCharm.class, GrizzlyBears.class, HillGiant.class})
    class ExileGraveyardMode {

        @Test
        @DisplayName("Empties the targeted player's graveyard")
        void exilesGraveyard() {
            harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }

        @Test
        void canExileOwnGraveyardWithoutExilingTheResolvingCharm() {
            GrizzlyBears ownCard = new GrizzlyBears();
            HillGiant opposingCard = new HillGiant();
            harness.setGraveyard(player1, List.of(ownCard));
            harness.setGraveyard(player2, List.of(opposingCard));
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .extracting(card -> card.getName()).containsExactly("Rakdos Charm");
            assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
            assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(ownCard.getId()));
        }

        @Test
        void canTargetAnEmptyGraveyard() {
            harness.setGraveyard(player2, List.of());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Rakdos Charm");
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    @CardUsed({RakdosCharm.class, Millstone.class, GrizzlyBears.class})
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new Millstone());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canDestroyOwnArtifactWithoutApplyingOtherModes() {
            harness.addToBattlefield(player1, new Millstone());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setGraveyard(player2, List.of(new GrizzlyBears()));
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player1, "Millstone"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Millstone");
            harness.assertInGraveyard(player1, "Millstone");
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }
    }

    @Nested
    @DisplayName("Mode 2: Each creature deals 1 damage to its controller")
    @CardUsed({RakdosCharm.class, GrizzlyBears.class, HillGiant.class, DaggerdromeImp.class})
    class DamageControllersMode {

        @Test
        @DisplayName("Each creature damages its own controller")
        void damagesControllers() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            int p1Life = gd.playerLifeTotals.get(player1.getId());
            int p2Life = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Life - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Life - 2);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @CardUsed({RakdosCharm.class, DaggerdromeImp.class})
        void lifelinkBelongsToEachCreatureRatherThanTheCharm() {
            harness.addToBattlefield(player1, new DaggerdromeImp());
            harness.addToBattlefield(player2, new DaggerdromeImp());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.assertOnBattlefield(player1, "Daggerdrome Imp");
            harness.assertOnBattlefield(player2, "Daggerdrome Imp");
        }

        @Test
        @CardUsed(RakdosCharm.class)
        void damageModeNeedsNoTargetOrCreatures() {
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.assertInGraveyard(player1, "Rakdos Charm");
        }
    }
}
