package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NayaSojourners.class, FlameJavelin.class, Forest.class, GrizzlyBears.class})
class NayaSojournersTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, puts a +1/+1 counter on target creature")
    void diesPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new NayaSojourners());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger targets only creatures, not lands")
    void deathTargetsCreaturesOnly() {
        harness.addToBattlefield(player1, new NayaSojourners());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Cycling puts a +1/+1 counter on target creature and draws a card")
    void cyclingPutsCounterOnTargetAndDraws() {
        harness.setHand(player1, List.of(new NayaSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // The cycling draw still happens: Naya Sojourners discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Naya Sojourners");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cyclingWithoutCreaturesStillDraws() {
        harness.setHand(player1, List.of(new NayaSojourners()));
        harness.setLibrary(player1, List.of(new NayaSojourners()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Naya Sojourners");
        harness.assertInHand(player1, "Naya Sojourners");
    }

    @Test
    void deathCounterCanBeDeclinedAfterChoosingTarget() {
        harness.addToBattlefield(player1, new NayaSojourners());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NayaSojourners());
        killWithFlameJavelin();
        harness.handlePermanentChosen(player1, target.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cyclingCounterCanBeDeclinedAndDrawStillResolves() {
        harness.setHand(player1, List.of(new NayaSojourners()));
        harness.setLibrary(player1, List.of(new NayaSojourners()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NayaSojourners());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Naya Sojourners");
    }

    @Test
    void cyclingCounterResolvesBeforeSeparateDrawAbility() {
        harness.setHand(player1, List.of(new NayaSojourners()));
        harness.setLibrary(player1, List.of(new NayaSojourners()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NayaSojourners());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInHand(player1, "Naya Sojourners");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Naya Sojourners");
    }

    @Test
    void losingCyclingTriggerTargetDoesNotPreventDraw() {
        harness.setHand(player1, List.of(new NayaSojourners()));
        harness.setLibrary(player1, List.of(new NayaSojourners()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, target.getId());

        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Naya Sojourners");
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID nayaId = harness.getPermanentId(player1, "Naya Sojourners");
        harness.castAndResolveInstant(player2, 0, nayaId);
    }
}
