package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenDemise.class, HillGiant.class, GrizzlyBears.class, Forest.class})
class GoldenDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, all creatures get -2/-2 until end of turn")
    void weakensAllCreaturesWithoutBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolve();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With the city's blessing, only opponents' creatures get -2/-2")
    void weakensOnlyOpposingCreaturesWithBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("Nine permanents plus the spell do not grant the city's blessing")
    void spellDoesNotCountAsTenthPermanent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without the blessing, creatures on either side die from zero toughness")
    void killsSmallCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A previously acquired blessing still protects creatures below ten permanents")
    void retainedBlessingProtectsOwnCreatures() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        castAndResolve();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ascend checks the permanent count at resolution, not at casting")
    void gainsBlessingWhenTenthPermanentArrivesBeforeResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new GoldenDemise(), "{1}{B}{B}");
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAndResolve();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(existingCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(laterCreature);
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new GoldenDemise(), "{1}{B}{B}");
        harness.passBothPriorities();
    }
}
