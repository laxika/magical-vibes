package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.c.CavernThoctar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrixisCharm.class, GrizzlyBears.class, FountainOfYouth.class, CavernThoctar.class, Mountain.class})
class GrixisCharmTest extends BaseCardTest {

    private void addUBR() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Nested
    @DisplayName("Mode 0: Return target permanent to its owner's hand")
    @CardUsed({GrixisCharm.class, FountainOfYouth.class})
    class BounceMode {

        @Test
        @DisplayName("Returns any permanent to its owner's hand")
        void returnsPermanent() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Fountain of Youth");
            harness.assertInHand(player2, "Fountain of Youth");
        }
    }

    @Nested
    @DisplayName("Mode 1: Target creature gets -4/-4 until end of turn")
    @CardUsed({GrixisCharm.class, GrizzlyBears.class, FountainOfYouth.class})
    class DebuffMode {

        @Test
        @DisplayName("Kills a 2/2 creature")
        void killsSmallCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control get +2/+0 until end of turn")
    @CardUsed({GrixisCharm.class, GrizzlyBears.class})
    class PumpMode {

        @Test
        @DisplayName("Boosts own creatures' power")
        void boostsOwnCreatures() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(bear.getEffectivePower()).isEqualTo(4);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Boost wears off at cleanup step")
        void boostWearsOff() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }
    }

    @Test
    @CardUsed({GrixisCharm.class, CavernThoctar.class})
    void survivingCreatureRecoversAfterCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new GrixisCharm()));
        addUBR();

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Cavern Thoctar");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @CardUsed({GrixisCharm.class, CavernThoctar.class})
    void teamBoostAffectsOnlyOwnCreaturesPresentAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CavernThoctar());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new GrixisCharm()));
        addUBR();

        harness.castInstant(player1, 0, 2, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new CavernThoctar());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new CavernThoctar());

        assertThat(first.getEffectivePower()).isEqualTo(7);
        assertThat(first.getEffectiveToughness()).isEqualTo(5);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(7);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponent.getEffectivePower()).isEqualTo(5);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(5);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(5);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @CardUsed({GrixisCharm.class})
    void teamBoostNeedsNoCreaturesOrTargets() {
        harness.setHand(player1, List.of(new GrixisCharm()));
        addUBR();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grixis Charm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({GrixisCharm.class, CavernThoctar.class})
    void debuffDoesNotFollowTargetReturnedToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        harness.setHand(player1, List.of(new GrixisCharm()));
        harness.setHand(player2, List.of(new GrixisCharm()));
        addUBR();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.castInstant(player2, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cavern Thoctar");
        harness.assertNotInGraveyard(player2, "Cavern Thoctar");
        harness.assertInGraveyard(player1, "Grixis Charm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({GrixisCharm.class, Mountain.class})
    void bounceCanTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new GrixisCharm()));
        addUBR();

        harness.castInstant(player1, 0, 0, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    @CardUsed({GrixisCharm.class, CavernThoctar.class})
    void bounceReturnsStolenCreatureToOwnerInsteadOfController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CavernThoctar());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        harness.setHand(player1, List.of(new GrixisCharm()));
        addUBR();

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cavern Thoctar");
        harness.assertInHand(player2, "Cavern Thoctar");
        harness.assertNotInHand(player1, "Cavern Thoctar");
    }
}
