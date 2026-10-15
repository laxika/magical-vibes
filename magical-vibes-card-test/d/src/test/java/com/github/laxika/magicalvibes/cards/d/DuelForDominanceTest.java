package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuelForDominance.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, MirriCatWarrior.class})
class DuelForDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Without coven: creatures fight with no +1/+1 counter")
    void withoutCovenCreaturesFightWithoutCounter() {
        // Only one creature you control — coven not met
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With coven: +1/+1 counter on your creature before fight")
    void withCovenPutsCounterBeforeFight() {
        // Elves 1/1, Bears 2/2, Giant 3/3 — three different powers = coven
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, theirElvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coven counter helps creature survive a fight it would otherwise lose")
    void covenCounterHelpsCreatureSurvive() {
        // Mirri 2/3 vs Giant 3/3: without counter Mirri dies; with counter Mirri is 3/4 and survives.
        // Board powers 1/2/3 (Elves/Mirri/Hill Giant) meet coven
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        UUID theirGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(mirriId, theirGiantId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mirri, Cat Warrior");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID id1 = bear1.getId();
        UUID id2 = bear2.getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void threeCreaturesWithOnlyTwoDifferentPowersDoNotMeetCoven() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void covenIsCheckedAtResolutionRatherThanCasting() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), opponent.getId()));
        harness.addToBattlefield(player1, new HillGiant());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void losingCovenBeforeResolutionStillAllowsFightWithoutCounter() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(elves);
        gd.playerGraveyards.get(player1.getId()).add(elves.getCard());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void missingOpponentTargetStillAllowsCovenCounterWithoutFight() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), opponent.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerGraveyards.get(player2.getId()).add(opponent.getCard());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void missingControlledTargetDoesNotDamageOpponentOrGiveItCounter() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(bear.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }
}
