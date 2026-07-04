package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GerminationPracticumTest extends BaseCardTest {

    @Test
    @DisplayName("Has PutCounterOnEachControlledPermanentEffect on creatures")
    void hasCorrectStructure() {
        GerminationPracticum card = new GerminationPracticum();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(PutCounterOnEachControlledPermanentEffect.class);
        PutCounterOnEachControlledPermanentEffect effect =
                (PutCounterOnEachControlledPermanentEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on each creature you control")
    void putsTwoCountersOnEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paradigm copy ceases to exist on resolution — not re-exiled or put into graveyard")
    void paradigmCopyCeasesToExist() {
        GerminationPracticum practicum = new GerminationPracticum();
        assumeTrue(practicum.getKeywords().contains(Keyword.PARADIGM),
                "Scryfall oracle must load Paradigm keyword");

        harness.setHand(player1, List.of(practicum));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Original resolved: exiled and a delayed paradigm trigger registered.
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Germination Practicum")).count()).isEqualTo(1);

        // Fire the beginning-of-precombat-main paradigm trigger for the active player.
        harness.forceActivePlayer(player1);
        ParadigmService paradigmService = GameTestEngineContext.get().getBean(ParadigmService.class);
        paradigmService.firePrecombatMainTriggers(gd);
        harness.passBothPriorities(); // resolve the trigger -> copy created in exile + may-cast prompt

        harness.handleMayAbilityChosen(player1, true); // cast the copy (no target)
        harness.passBothPriorities(); // resolve the copy
        harness.passBothPriorities();

        // The copy ceased to exist: exactly the original remains in exile (not two), and nothing
        // named Germination Practicum landed in the graveyard or is stuck on the stack.
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Germination Practicum")).count()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Germination Practicum"));
        assertThat(gd.stack.stream().anyMatch(e -> e.getCard() != null
                && e.getCard().getName().equals("Germination Practicum"))).isFalse();
    }
}
