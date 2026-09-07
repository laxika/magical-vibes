package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Chaoslace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hydroblast.class, HillGiant.class, GrizzlyBears.class})
class HydroblastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target spell if it's red")
    class CounterMode {

        @Test
        @DisplayName("Counters a red spell")
        void countersRedSpell() {
            HillGiant giant = new HillGiant();
            harness.setHand(player1, List.of(giant));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.setHand(player2, List.of(new Hydroblast()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, giant.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Hill Giant"));
            harness.assertInGraveyard(player1, "Hill Giant");
            harness.assertNotOnBattlefield(player1, "Hill Giant");
        }

        @Test
        @DisplayName("Does nothing to a non-red spell (it resolves)")
        void doesNothingToNonRedSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Hydroblast()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, bears.getId());
            harness.passBothPriorities(); // Hydroblast resolves, does nothing
            harness.passBothPriorities(); // Grizzly Bears resolves onto the battlefield

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Hydroblast");
        }
    }

    @Test
    @CardUsed(Chaoslace.class)
    @DisplayName("Counter mode checks the target spell's color when it resolves")
    void counterModeChecksTargetColorAtResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        Chaoslace chaoslace = new Chaoslace();
        harness.setHand(player1, List.of(bears, chaoslace));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Hydroblast()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chaoslace");
        harness.assertInGraveyard(player2, "Hydroblast");
    }

    @Nested
    @DisplayName("Mode 1: Destroy target permanent if it's red")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a red permanent")
        void destroysRedPermanent() {
            harness.addToBattlefield(player2, new HillGiant());

            harness.setHand(player1, List.of(new Hydroblast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Hill Giant"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Hill Giant");
            harness.assertInGraveyard(player2, "Hill Giant");
        }

        @Test
        @DisplayName("Does nothing to a non-red permanent")
        void doesNothingToNonRedPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Hydroblast()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Hydroblast");
        }
    }

    @Test
    @CardUsed(Chaoslace.class)
    @DisplayName("Destroy mode checks the target permanent's color when it resolves")
    void destroyModeChecksTargetColorAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Hydroblast(), new Chaoslace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 1, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chaoslace");
        harness.assertInGraveyard(player1, "Hydroblast");
    }
}
