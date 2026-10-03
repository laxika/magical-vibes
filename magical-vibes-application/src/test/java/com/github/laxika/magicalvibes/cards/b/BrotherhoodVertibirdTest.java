package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrotherhoodVertibird.class, GrizzlyBears.class, Ornithopter.class})
class BrotherhoodVertibirdTest extends BaseCardTest {

    @Test
    void powerEqualsControlledArtifactsAndToughnessIsFour() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vertibird)).isEqualTo(4);

        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(4);
    }

    @Test
    void crewAnimatesVertibirdAndTapsCreaturesWithTotalPowerTwo() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vertibird)).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void powerContinuesToUpdateWhileCrewed() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vertibird)).isEqualTo(4);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vertibird)).isEqualTo(4);
    }

    @Test
    void summoningSickCreatureCanCrewTappedVehicleAndAnimationExpires() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        vertibird.tap();

        assertThat(gqs.isCreature(gd, vertibird)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vertibird)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vertibird)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, vertibird)).isFalse();
        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(1);
    }

    @Test
    void opponentsCreaturesAndZeroPowerCreaturesCannotSupplyCrewTwo() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(thopter.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, vertibird)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerDefiningAbilityWorksInHandAndGraveyardWithoutCountingItself() {
        BrotherhoodVertibird vertibird = new BrotherhoodVertibird();
        harness.setHand(player1, List.of(vertibird));
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectiveCardPower(gd, vertibird)).isZero();
        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectiveCardPower(gd, vertibird)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(vertibird));
        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectiveCardPower(gd, vertibird)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, vertibird)).isEqualTo(4);
    }
}
