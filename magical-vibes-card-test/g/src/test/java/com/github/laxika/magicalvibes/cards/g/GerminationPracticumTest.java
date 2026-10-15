package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerminationPracticum.class, GrizzlyBears.class, Twincast.class})
class GerminationPracticumTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on each creature you control")
    void putsTwoCountersOnEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Does not affect opponent creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent opponentBear = findPermanent(player2, "Grizzly Bears");
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paradigm copy ceases to exist on resolution — not re-exiled or put into graveyard")
    void paradigmCopyCeasesToExist() {
        GerminationPracticum practicum = new GerminationPracticum();
        harness.setHand(player1, List.of(practicum));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Original resolved: exiled and a delayed paradigm trigger registered.
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Germination Practicum")).count()).isEqualTo(1);

        // Fire the beginning-of-precombat-main paradigm trigger for the active player.
        harness.forceActivePlayer(player1);
        ParadigmService paradigmService = GameTestEngineContext.get().getBean(ParadigmService.class);
        harness.inMutationScope(() -> paradigmService.firePrecombatMainTriggers(gd));
        harness.passBothPriorities(); // resolve the trigger -> copy created in exile + may-cast prompt

        harness.handleMayAbilityChosen(player1, true); // cast the copy (no target)
        harness.passBothPriorities(); // resolve the copy
        harness.passBothPriorities();

        // The copy ceased to exist: exactly the original remains in exile (not two), and nothing
        // named Germination Practicum landed in the graveyard or is stuck on the stack.
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Germination Practicum")).count()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Germination Practicum");
        assertThat(gd.stack.stream().anyMatch(e -> e.getCard() != null
                && e.getCard().getName().equals("Germination Practicum"))).isFalse();
    }

    @Test
    @DisplayName("Resolving an opponent's spell copy grants that copy's controller paradigm")
    void firstResolutionCanBeASpellCopy() {
        GerminationPracticum practicum = new GerminationPracticum();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(practicum));
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, practicum.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.forceActivePlayer(player2);
        ParadigmService paradigmService = GameTestEngineContext.get().getBean(ParadigmService.class);
        harness.inMutationScope(() -> paradigmService.firePrecombatMainTriggers(gd));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Paradigm can be declined and still offers a free copy on a later turn")
    void decliningDoesNotRemoveRecurringAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        ParadigmService paradigmService = GameTestEngineContext.get().getBean(ParadigmService.class);
        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> paradigmService.firePrecombatMainTriggers(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.exiledCards).hasSize(1);

        harness.inMutationScope(() -> paradigmService.firePrecombatMainTriggers(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
    }

    @Test
    @DisplayName("Multiple originals resolve and exile but grant only one recurring copy per main phase")
    void multipleOriginalsGrantOnlyOneRecurringCopy() {
        harness.setHand(player1, List.of(new GerminationPracticum(), new GerminationPracticum()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.exiledCards).hasSize(2);
        harness.assertNotInGraveyard(player1, "Germination Practicum");
        harness.forceActivePlayer(player1);
        ParadigmService paradigmService = GameTestEngineContext.get().getBean(ParadigmService.class);
        harness.inMutationScope(() -> paradigmService.firePrecombatMainTriggers(gd));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.exiledCards).hasSize(2);
    }
}
