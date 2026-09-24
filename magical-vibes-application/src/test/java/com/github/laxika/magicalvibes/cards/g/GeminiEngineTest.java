package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeminiEngine.class, JaceBeleren.class})
class GeminiEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates an untapped colorless artifact Twin with the source's current power and toughness")
    void attackCreatesTwinWithCurrentPowerAndToughness() {
        Permanent engine = addCreatureReady(player1, new GeminiEngine());
        engine.setPowerModifier(2);
        engine.setToughnessModifier(1);
        Permanent secondEngine = addCreatureReady(player1, new GeminiEngine());
        secondEngine.setPowerModifier(2);
        secondEngine.setToughnessModifier(1);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        List<Permanent> twins = findPermanents(player1, "Twin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(twins).hasSize(2);
        assertThat(twins).allSatisfy(twin -> {
            assertThat(twin.getCard().getName()).isEqualTo("Twin");
            assertThat(twin.getCard().isToken()).isTrue();
            assertThat(twin.getCard().getColors()).isEmpty();
            assertThat(twin.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(twin.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(twin.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
            assertThat(twin.isAttacking()).isTrue();
            assertThat(twin.isTapped()).isFalse();
            assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, twin)).isEqualTo(5);
        });
    }

    @Test
    @DisplayName("Twin keeps the power and toughness it had when it was created")
    void twinKeepsPowerAndToughnessAfterSourceChanges() {
        Permanent engine = addCreatureReady(player1, new GeminiEngine());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            engine.setPowerModifier(2);
            engine.setToughnessModifier(1);
            resolveAllTriggers();
        });

        Permanent twin = findPermanents(player1, "Twin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, twin)).isEqualTo(5);

        engine.setPowerModifier(7);
        engine.setToughnessModifier(7);

        assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, twin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Twin lets its controller choose an opposing player or planeswalker to attack")
    void twinCanAttackOpposingPlaneswalker() {
        addCreatureReady(player1, new GeminiEngine());
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(jace);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput())
                    .as("Gemini Engine should ask for the Twin's attack target")
                    .isTrue();
            harness.handlePermanentChosen(player1, jace.getId());
        });

        Permanent twin = findPermanents(player1, "Twin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(twin.getAttackTarget()).isEqualTo(jace.getId());
    }

    @Test
    @DisplayName("Twin is sacrificed at end of combat")
    void twinIsSacrificedAtEndOfCombat() {
        addCreatureReady(player1, new GeminiEngine());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Twin")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Twin")).noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gameLogContains("Twin")).isTrue();
    }
}
