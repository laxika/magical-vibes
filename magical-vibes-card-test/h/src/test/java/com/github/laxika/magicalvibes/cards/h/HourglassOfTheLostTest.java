package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HourglassOfTheLost.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        MindStone.class, Plains.class, Ornithopter.class})
class HourglassOfTheLostTest extends BaseCardTest {

    @Test
    void tappingAddsWhiteManaAndATimeCounter() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(manaBefore + 1);
        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void returnsEachNonlandPermanentWithTheChosenManaValue() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 3);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new MindStone(), new HillGiant(), new Plains(), new LightningBolt()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Hill Giant", "Plains", "Lightning Bolt");
        harness.assertNotOnBattlefield(player1, "Hourglass of the Lost");
    }

    @Test
    void returnsAllMatchingCardsEvenWhenThereAreMoreThanX() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new MindStone()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroCountersReturnsZeroManaNonlandPermanentsButNotLands() {
        harness.addToBattlefield(player1, new HourglassOfTheLost());
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Ornithopter(), new Plains()));

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ornithopter")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        harness.assertNotOnBattlefield(player1, "Hourglass of the Lost");
    }

    @Test
    void paysCountersAndExilesBeforeResolutionAndReturnsOnlyControllersCardsUntapped() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 3);
        harness.setGraveyard(player1, List.of(new MindStone()));
        harness.setGraveyard(player2, List.of(new MindStone()));

        harness.activateAbility(player1, 0, 1, 2, null);

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Hourglass of the Lost");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Hourglass of the Lost"));
        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    void cannotRemoveMoreCountersThanAvailable() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(hourglass.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hourglass of the Lost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnAbilityCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new HourglassOfTheLost());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hourglass of the Lost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnAbilityCannotBeActivatedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HourglassOfTheLost());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hourglass of the Lost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnAbilityCannotBeActivatedWhileStackIsNotEmpty() {
        harness.addToBattlefield(player1, new HourglassOfTheLost());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hourglass of the Lost");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void manaAbilityResolvesImmediatelyAndCannotBeUsedAgainWhileTapped() {
        Permanent hourglass = harness.addToBattlefieldAndReturn(player1, new HourglassOfTheLost());
        hourglass.setCounterCount(CounterType.TIME, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hourglass.isTapped()).isTrue();
        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hourglass.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Hourglass of the Lost");
    }
}
