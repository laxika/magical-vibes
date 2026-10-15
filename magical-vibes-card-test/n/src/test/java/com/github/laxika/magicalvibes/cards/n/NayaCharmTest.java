package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NayaCharm.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
class NayaCharmTest extends BaseCardTest {

    // Mode indices: 0 = 3 damage to target creature, 1 = return target card from a graveyard to
    //               its owner's hand, 2 = tap all creatures target player controls.

    private void addRGW() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Nested
    @CardUsed({NayaCharm.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
    @DisplayName("Mode 0: Naya Charm deals 3 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Kills a 2/2")
        void dealsThreeDamage() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Fountain of Youth")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void killsCreatureWithExactlyThreeToughness() {
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Hill Giant"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Hill Giant");
            harness.assertInGraveyard(player2, "Hill Giant");
        }

        @Test
        void cannotTargetPlayerForDamage() {
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({NayaCharm.class, GrizzlyBears.class, FountainOfYouth.class})
    @DisplayName("Mode 1: Return target card from a graveyard to its owner's hand")
    class GraveyardReturnMode {

        @Test
        @DisplayName("Returns an opponent's graveyard card to the opponent's hand")
        void returnsToOwnersHand() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player2, "Grizzly Bears");
            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertNotInHand(player1, "Grizzly Bears");
        }

        @Test
        void returnsNoncreatureFromOwnGraveyard() {
            Card artifact = new FountainOfYouth();
            harness.setGraveyard(player1, List.of(artifact));
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 1, artifact.getId());
            harness.passBothPriorities();

            harness.assertInHand(player1, "Fountain of Youth");
            harness.assertNotInGraveyard(player1, "Fountain of Youth");
            harness.assertInGraveyard(player1, "Naya Charm");
        }

        @Test
        void doesNotReturnCardThatLeavesGraveyardBeforeResolution() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.setGraveyard(player2, List.of());
            harness.setExile(player2, List.of(bears));
            harness.passBothPriorities();

            harness.assertNotInHand(player2, "Grizzly Bears");
            harness.assertNotInHand(player1, "Grizzly Bears");
            assertThat(gd.findExiledCard(bears.getId())).isNotNull();
            harness.assertInGraveyard(player1, "Naya Charm");
        }
    }

    @Nested
    @CardUsed({NayaCharm.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
    @DisplayName("Mode 2: Tap all creatures target player controls")
    class TapMode {

        @Test
        @DisplayName("Taps every creature the targeted player controls")
        void tapsTargetPlayersCreatures() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new HillGiant());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .allMatch(Permanent::isTapped);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(Permanent::isTapped);
        }

        @Test
        void canTargetSelfAndLeavesNoncreaturesUntapped() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 2, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .singleElement().matches(Permanent::isTapped);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Fountain of Youth"))
                    .singleElement().matches(p -> !p.isTapped());
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isTapped);
        }

        @Test
        void canTargetPlayerWithNoCreatures() {
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Naya Charm");
            assertThat(gd.stack).isEmpty();
        }
    }
}
