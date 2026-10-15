package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowderKeg.class, GrizzlyBears.class, MindStone.class, LlanowarElves.class,
        Forest.class, Ornithopter.class})
class PowderKegTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a fuse counter on Powder Keg")
    void upkeepAcceptedAddsFuseCounter() {
        Permanent keg = addReadyKeg(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(keg.getCounterCount(CounterType.FUSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves fuse counters unchanged")
    void upkeepDeclinedAddsNoFuseCounter() {
        Permanent keg = addReadyKeg(player1);
        keg.setCounterCount(CounterType.FUSE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(keg.getCounterCount(CounterType.FUSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Powder Keg triggers only during its controller's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        Permanent keg = addReadyKeg(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(keg.getCounterCount(CounterType.FUSE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Powder Keg destroys matching artifacts and creatures on both sides")
    void destroysMatchingArtifactsAndCreatures() {
        Permanent keg = addReadyKeg(player1);
        keg.setCounterCount(CounterType.FUSE, 2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Powder Keg");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Mind Stone");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("With no fuse counters, Powder Keg destroys only zero-mana artifacts and creatures")
    void zeroFuseCountersDestroyZeroManaArtifactsAndCreatures() {
        addReadyKeg(player1);

        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Powder Keg");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Powder Keg's destruction ability requires it to be untapped")
    void cannotActivateTappedKeg() {
        Permanent keg = addReadyKeg(player1);
        keg.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zero fuse counters destroy a face-down creature regardless of its front-face cost")
    void zeroFuseCountersDestroyFaceDownCreature() {
        addReadyKeg(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Two fuse counters spare a face-down creature with a two-mana front face")
    void twoFuseCountersSpareFaceDownCreature() {
        Permanent keg = addReadyKeg(player1);
        keg.setCounterCount(CounterType.FUSE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("A newly entered noncreature Powder Keg can activate and is sacrificed before resolution")
    void newlyEnteredKegCanActivateAndPaysSacrificeImmediately() {
        harness.addToBattlefield(player1, new PowderKeg());
        harness.addToBattlefield(player2, new Ornithopter());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Powder Keg");
        harness.assertNotOnBattlefield(player1, "Powder Keg");
        harness.assertOnBattlefield(player2, "Ornithopter");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
    }

    private Permanent addReadyKeg(Player owner) {
        return addCreatureReady(owner, new PowderKeg());
    }
}
