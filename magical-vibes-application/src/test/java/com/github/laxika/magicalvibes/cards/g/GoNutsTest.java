package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoNuts.class, GrizzlyBears.class, HillGiant.class})
class GoNutsTest extends BaseCardTest {

    @Test
    void counterModePutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), List.of());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void fightModeMakesControlledCreatureFightOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(ownCreature.getId(), opposingCreature.getId()), List.of());

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void teamworkResolvesBothModesAndTapsChosenCreatures() {
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast(new int[]{0, 1}, List.of(
                counterTarget.getId(), counterTarget.getId(), opposingCreature.getId()),
                List.of(teammate.getId()));

        assertThat(counterTarget.getEffectivePower()).isEqualTo(3);
        assertThat(counterTarget.getEffectiveToughness()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void fightModeRequiresFirstTargetToBeControlled() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndTaps(
                player1, 0, 1, 2, new int[]{1},
                List.of(opposingCreature.getId(), ownCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, List<java.util.UUID> teamworkIds) {
        prepareCard();
        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 2, modes, targetIds, teamworkIds);
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new GoNuts()));
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
