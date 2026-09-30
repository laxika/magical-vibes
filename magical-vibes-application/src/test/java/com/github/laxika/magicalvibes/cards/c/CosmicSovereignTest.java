package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardPrinting;
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

@CardUsed({CosmicSovereign.class, CentaurCourser.class})
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
