package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.service.effect.normalfx.ConjureRandomCreatureWithManaValueEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicSovereign.class, CentaurCourser.class, ColossalDreadmaw.class, Oakenform.class})
class CosmicSovereignTest extends BaseCardTest {

    @Test
    void conjuresACreatureWithTheSourcePowerAndExilesItAtTheNextEndStep() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());

        // A random zero-toughness creature could die before this test can inspect it.
        var handler = GameTestEngineContext.get().getBean(ConjureRandomCreatureWithManaValueEffectHandler.class);
        @SuppressWarnings("unchecked")
        Map<Integer, List<CardPrinting>> candidates = (Map<Integer, List<CardPrinting>>)
                ReflectionTestUtils.getField(handler, "candidatesByManaValue");
        List<CardPrinting> previous = candidates.put(3, List.of(new CardPrinting(
                "M19", "171", CentaurCourser.class.getName(), "CentaurCourser", false, CentaurCourser::new)));
        try {
            resolveBeginningOfCombat(player1);
        } finally {
            if (previous == null) {
                candidates.remove(3);
            } else {
                candidates.put(3, previous);
            }
        }

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(sovereign.getId()))
                .toList();
        assertThat(conjured).hasSize(1);
        Permanent creature = conjured.getFirst();
        assertThat(creature.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(creature.getCard().getManaValue()).isEqualTo(gqs.getEffectivePower(gd, sovereign));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(creature.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_AT_END_STEP);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getCard().getId()));
    }

    @Test
    void redActivationBoostsPowerUntilEndOfTurn() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(3);
    }

    @Test
    void doesNotConjureAtTheBeginningOfAnOpponentsCombat() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());

        resolveBeginningOfCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(sovereign);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    void usesPowerAtResolutionAfterActivatingInResponseToTheCombatTrigger() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        withConjureCandidate(5, new CardPrinting("YEOE", "14", CosmicSovereign.class.getName(),
                "CosmicSovereign", false, CosmicSovereign::new), () ->
                harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
                    harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
                    assertThat(gd.stack).hasSize(1);
                    harness.addMana(player1, ManaColor.RED, 2);
                    harness.activateAbility(player1, 0, null, null);
                    harness.passBothPriorities();
                    harness.activateAbility(player1, 0, null, null);
                    harness.passBothPriorities();
                    assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(5);
                    resolveAllTriggers();
                }));

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(sovereign.getId())).toList();
        assertThat(conjured).hasSize(1);
        assertThat(conjured.getFirst().getCard()).isInstanceOf(CosmicSovereign.class);
        assertThat(conjured.getFirst().getCard().getOwnerId()).isEqualTo(player1.getId());
        assertThat(conjured.getFirst().getCard().isToken()).isFalse();
    }

    @Test
    void usesLastKnownPowerIncludingAnAuraWhenTheSourceLeavesBeforeResolution() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Oakenform());
        aura.setAttachedTo(sovereign.getId());
        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(6);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        withConjureCandidate(6, new CardPrinting("M19", "172", ColossalDreadmaw.class.getName(),
                "ColossalDreadmaw", false, ColossalDreadmaw::new), () ->
                harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
                    harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
                    assertThat(gd.stack).hasSize(1);
                    harness.inMutationScope(() -> harness.getPermanentRemovalService()
                            .removePermanentToGraveyard(gd, sovereign));
                    harness.runStateBasedActions();
                    assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
                    resolveAllTriggers();
                }));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent conjured = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(conjured.getCard()).isInstanceOf(ColossalDreadmaw.class);
        assertThat(gqs.hasKeyword(gd, conjured, Keyword.HASTE)).isTrue();
    }

    private void withConjureCandidate(int manaValue, CardPrinting printing, Runnable action) {
        var handler = GameTestEngineContext.get().getBean(ConjureRandomCreatureWithManaValueEffectHandler.class);
        @SuppressWarnings("unchecked")
        Map<Integer, List<CardPrinting>> candidates = (Map<Integer, List<CardPrinting>>)
                ReflectionTestUtils.getField(handler, "candidatesByManaValue");
        List<CardPrinting> previous = candidates.put(manaValue, List.of(printing));
        try {
            action.run();
        } finally {
            if (previous == null) {
                candidates.remove(manaValue);
            } else {
                candidates.put(manaValue, previous);
            }
        }
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
            resolveAllTriggers();
        });
    }
}
