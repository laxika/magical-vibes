package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawningPod.class, GrizzlyBears.class, LlanowarElves.class})
class SpawningPodTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndSeeksCreatureWithOneHigherManaValue() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        Permanent sought = findPermanent(player1, "Grizzly Bears");
        assertThat(sought.getGrantedSubtypes()).contains(CardSubtype.PHYREXIAN);
    }

    @Test
    void doesNotPutCreatureOntoBattlefieldWhenManaValueDoesNotMatch() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void paysCostsBeforeResolutionAndPutsExactlyOneMatchingCreatureOntoBattlefield() {
        Permanent pod = harness.addToBattlefieldAndReturn(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        LlanowarElves first = new LlanowarElves();
        LlanowarElves last = new LlanowarElves();
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, bear1, bear2, last));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pod.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(first);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(last);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canSacrificeTappedCreatureAndResolveWithEmptyLibrary() {
        harness.addToBattlefield(player1, new SpawningPod());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.tap();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsCreatureToPayCost() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedPod() {
        Permanent pod = harness.addToBattlefieldAndReturn(player1, new SpawningPod());
        pod.tap();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesManaValueOfChosenSacrificeAndDoesNotSeekNoncreature() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SpawningPod noncreature = new SpawningPod();
        harness.setLibrary(player1, List.of(noncreature, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spawning Pod")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnStack() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Spawning Pod").get(1).isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
