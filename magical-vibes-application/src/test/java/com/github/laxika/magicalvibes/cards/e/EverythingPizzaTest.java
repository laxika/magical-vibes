package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverythingPizza.class, Forest.class, GrizzlyBears.class})
class EverythingPizzaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and searches for a basic land")
    void entersAndSearchesForBasicLand() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new EverythingPizza()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Everything Pizza");
    }

    @Test
    @DisplayName("Activated ability resolves each instruction against its target group")
    void activatedAbilityResolvesEveryInstruction() {
        harness.addToBattlefield(player1, new EverythingPizza());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Everything Pizza");
        harness.assertLife(player2, 23);
        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated ability allows omitting the creature target")
    void activatedAbilityAllowsOmittingCreatureTarget() {
        harness.addToBattlefield(player1, new EverythingPizza());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 17);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
