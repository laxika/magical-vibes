package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianConjurer;
import com.github.laxika.magicalvibes.cards.c.Chaoslace;
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

@CardUsed({Pyroblast.class, BalduvianConjurer.class, PaleBears.class})
class PyroblastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target spell if it's blue")
    class CounterMode {

        @Test
        @DisplayName("Counters a blue spell")
        void countersBlueSpell() {
            BalduvianConjurer conjurer = new BalduvianConjurer();
            harness.setHand(player1, List.of(conjurer));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.setHand(player2, List.of(new Pyroblast()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, conjurer.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Balduvian Conjurer"));
            harness.assertInGraveyard(player1, "Balduvian Conjurer");
            harness.assertNotOnBattlefield(player1, "Balduvian Conjurer");
        }

        @Test
        @DisplayName("Does nothing to a non-blue spell (it resolves)")
        void doesNothingToNonBlueSpell() {
            PaleBears bears = new PaleBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setHand(player2, List.of(new Pyroblast()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, bears.getId());
            harness.passBothPriorities(); // Pyroblast resolves, does nothing
            harness.passBothPriorities(); // Pale Bears resolves onto the battlefield

            harness.assertOnBattlefield(player1, "Pale Bears");
            harness.assertInGraveyard(player2, "Pyroblast");
        }

        @Test
        @CardUsed(Chaoslace.class)
        @DisplayName("Checks the target spell's color when it resolves")
        void checksTargetSpellColorAtResolution() {
            BalduvianConjurer conjurer = new BalduvianConjurer();
            Chaoslace chaoslace = new Chaoslace();
            harness.setHand(player1, List.of(conjurer, chaoslace));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.setHand(player2, List.of(new Pyroblast()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, conjurer.getId());
            harness.castInstant(player1, 0, conjurer.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Balduvian Conjurer");
            harness.assertInGraveyard(player1, "Chaoslace");
            harness.assertInGraveyard(player2, "Pyroblast");
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target permanent if it's blue")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            harness.addToBattlefield(player2, new BalduvianConjurer());

            harness.setHand(player1, List.of(new Pyroblast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Balduvian Conjurer"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Balduvian Conjurer");
            harness.assertInGraveyard(player2, "Balduvian Conjurer");
        }

        @Test
        @DisplayName("Does nothing to a non-blue permanent")
        void doesNothingToNonBluePermanent() {
            harness.addToBattlefield(player2, new PaleBears());

            harness.setHand(player1, List.of(new Pyroblast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Pale Bears"));
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Pale Bears");
            harness.assertInGraveyard(player1, "Pyroblast");
        }

        @Test
        @CardUsed(Chaoslace.class)
        @DisplayName("Checks the target permanent's color when it resolves")
        void checksTargetPermanentColorAtResolution() {
            harness.addToBattlefield(player2, new BalduvianConjurer());
            UUID targetId = harness.getPermanentId(player2, "Balduvian Conjurer");

            harness.setHand(player1, List.of(new Pyroblast(), new Chaoslace()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 1, targetId);
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Balduvian Conjurer");
            harness.assertInGraveyard(player1, "Chaoslace");
            harness.assertInGraveyard(player1, "Pyroblast");
        }
    }
}
