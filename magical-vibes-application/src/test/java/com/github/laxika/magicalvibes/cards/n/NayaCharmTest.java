package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NayaCharmTest extends BaseCardTest {

    // Mode indices: 0 = 3 damage to target creature, 1 = return target card from a graveyard to
    //               its owner's hand, 2 = tap all creatures target player controls.

    private void addRGW() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Naya Charm deals 3 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Kills a 2/2, a 3/3 survives")
        void dealsThreeDamage() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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
    }

    @Nested
    @DisplayName("Mode 1: Return target card from a graveyard to its owner's hand")
    class GraveyardReturnMode {

        @Test
        @DisplayName("Returns an opponent's graveyard card to the opponent's hand")
        void returnsToOwnersHand() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
            harness.setHand(player1, List.of(new NayaCharm()));
            addRGW();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    @Nested
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
    }
}
