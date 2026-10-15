package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercitysEmbrace.class, AirElemental.class, GrizzlyBears.class})
class UndercitysEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent sacrifices a creature and controller gains 4 life with a power-4 creature")
    void sacrificesAndGainsLifeWithPowerFourCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Target opponent sacrifices a creature without the life gain when controller has no power-4 creature")
    void sacrificesWithoutGainingLifeBelowPowerFour() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void gainsLifeEvenWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertInGraveyard(player1, "Undercity's Embrace");
    }

    @Test
    void multipleQualifyingCreaturesStillGainOnlyFourLife() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    void combinedPowerOfSmallCreaturesDoesNotEnableLifeGain() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void opponentChoosesSacrificeBeforeControllerGainsLife() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player1, lifeBefore);
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertLife(player1, lifeBefore + 4);
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingPowerFourCreatureDoesNotEnableLifeGain() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void powerThreeDoesNotEnableLifeGain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void checksEffectivePowerAtResolutionRatherThanCasting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    void losingPowerFourBeforeResolutionPreventsLifeGain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new UndercitysEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }
}
