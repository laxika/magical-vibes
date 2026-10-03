package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AutarchMammoth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hulldrifter;
import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendTheRider.class, Plains.class, GrizzlyBears.class, DuskLegionDreadnought.class,
        Hulldrifter.class, LumberingWorldwagon.class, AutarchMammoth.class})
class DefendTheRiderTest extends BaseCardTest {

    @Test
    void grantsHexproofAndIndestructibleToTargetPermanentUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetPermanentControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsPilotThatContributesTwoAdditionalPowerToCrew() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        harness.setHand(player1, List.of(new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent pilot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    void onePilotCanCrewThreeWithoutIncreasingItsOrdinaryPower() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Hulldrifter());
        harness.setHand(player1, List.of(new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent pilot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);
        assertThat(pilot.getCard().getColors()).isEmpty();
        assertThat(pilot.getCard().getSubtypes()).contains(CardSubtype.PILOT);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    void onePilotCannotCrewFour() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LumberingWorldwagon());
        harness.setHand(player1, List.of(new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void twoNewPilotsCanPaySaddleFive() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new AutarchMammoth());
        harness.setHand(player1, List.of(new DefendTheRider(), new DefendTheRider()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        List<Permanent> pilots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(pilots).hasSize(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mount), null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilots).allSatisfy(pilot -> {
            assertThat(pilot.isTapped()).isTrue();
            assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        });
    }
}
