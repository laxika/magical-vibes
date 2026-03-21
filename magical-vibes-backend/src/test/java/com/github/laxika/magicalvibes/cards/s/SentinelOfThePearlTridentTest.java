package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KembaKhaRegent;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelOfThePearlTridentTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Card has MayEffect wrapping ExileTargetPermanentAndReturnAtEndStepEffect on ETB")
    void hasCorrectEffects() {
        SentinelOfThePearlTrident card = new SentinelOfThePearlTrident();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ExileTargetPermanentAndReturnAtEndStepEffect.class);
    }

    @Test
    @DisplayName("Target filter requires historic permanent controlled by source controller")
    void hasCorrectTargetFilter() {
        SentinelOfThePearlTrident card = new SentinelOfThePearlTrident();

        assertThat(card.getSpellTargets()).hasSize(1);
        assertThat(card.getSpellTargets().getFirst().getFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) card.getSpellTargets().getFirst().getFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentAllOfPredicate.class);
        PermanentAllOfPredicate allOf = (PermanentAllOfPredicate) filter.predicate();
        assertThat(allOf.predicates()).hasSize(2);
        assertThat(allOf.predicates()).anySatisfy(p ->
                assertThat(p).isInstanceOf(PermanentControlledBySourceControllerPredicate.class));
        assertThat(allOf.predicates()).anySatisfy(p ->
                assertThat(p).isInstanceOf(PermanentIsHistoricPredicate.class));
    }

    // ===== ETB with artifact (historic) =====

    @Test
    @DisplayName("Can exile an artifact you control (historic)")
    void canExileOwnArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept
        harness.handlePermanentChosen(player1, ornithopterId); // choose Ornithopter

        // Ornithopter should be exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    // ===== ETB with legendary creature (historic) =====

    @Test
    @DisplayName("Can exile a legendary creature you control (historic)")
    void canExileLegendaryCreature() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, kembaId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kemba, Kha Regent"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kemba, Kha Regent"));
    }

    // ===== May ability =====

    @Test
    @DisplayName("Resolving triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sentinel of the Pearl Trident"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
    }

    // ===== Return at end step =====

    @Test
    @DisplayName("Exiled permanent returns at beginning of next end step")
    void exiledPermanentReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ornithopterId);

        // Ornithopter is exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));

        // Advance to end step
        advanceToEndStep();

        // Ornithopter should be back on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, kembaId);

        advanceToEndStep();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kemba, Kha Regent"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a non-historic creature you control")
    void cannotTargetNonHistoricCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        // May prompt appears, but declining leaves Grizzly Bears untouched
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sentinel of the Pearl Trident"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target opponent's historic permanent")
    void cannotTargetOpponentHistoricPermanent() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        // May prompt appears, but declining leaves Ornithopter untouched
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
    }

    // ===== Helpers =====

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
