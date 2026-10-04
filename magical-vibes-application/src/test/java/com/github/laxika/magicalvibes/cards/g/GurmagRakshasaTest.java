package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GurmagRakshasa.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class GurmagRakshasaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -2/-2 and your creature +2/+2")
    void etbAppliesBothModifiers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = ownCreature.getId();
        UUID opposingCreatureId = opposingCreature.getId();
        harness.castCreature(player1, 0, List.of(opposingCreatureId, ownCreatureId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = ownCreature.getId();
        UUID opposingCreatureId = opposingCreature.getId();
        harness.castCreature(player1, 0, List.of(opposingCreatureId, ownCreatureId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each target must have the required controller")
    void enforcesTargetControllers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID ownCreatureId = ownCreature.getId();
        UUID opposingCreatureId = opposingCreature.getId();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownCreatureId, opposingCreatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rakshasa can target itself after entering the battlefield")
    void canTargetItself() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GurmagRakshasa());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent rakshasa = findPermanent(player1, "Gurmag Rakshasa");
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.handlePermanentChosen(player1, rakshasa.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rakshasa)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, rakshasa)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rakshasa can be cast without an opposing creature and its trigger is omitted")
    void noOpposingCreatureOmitsTrigger() {
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gurmag Rakshasa");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent rakshasa = findPermanent(player1, "Gurmag Rakshasa");
        assertThat(gqs.getEffectivePower(gd, rakshasa)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rakshasa)).isEqualTo(5);
    }

    @Test
    @DisplayName("Zero toughness kills the opposing creature while the friendly creature is boosted")
    void reductionKillsTwoToughnessCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GurmagRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(opposingCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("The surviving target receives only its own modifier when the other target is bounced")
    void resolvesForRemainingTarget(boolean bounceOpponent) {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GurmagRakshasa(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, List.of(opposingCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                bounceOpponent ? opposingCreature.getId() : ownCreature.getId());
        harness.passBothPriorities();

        if (bounceOpponent) {
            harness.assertInHand(player2, "Hill Giant");
            assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        } else {
            harness.assertInHand(player1, "Grizzly Bears");
            assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        }
    }
}
