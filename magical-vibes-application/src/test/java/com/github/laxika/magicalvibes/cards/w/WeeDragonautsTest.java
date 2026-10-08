package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.p.PrimevalLight;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeeDragonauts.class, Pyromatics.class, GhostWarden.class, PrimevalLight.class})
class WeeDragonautsTest extends BaseCardTest {

    private Permanent addDragonauts() {
        Permanent dragonauts = harness.addToBattlefieldAndReturn(player1, new WeeDragonauts());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return dragonauts;
    }

    @Test
    @DisplayName("Gets +2/+0 when you cast an instant")
    void pumpsWhenInstantCast() {
        Permanent dragonauts = addDragonauts();

        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(2);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when you cast a sorcery")
    void pumpsWhenSorceryCast() {
        Permanent dragonauts = addDragonauts();

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(2);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not pump when you cast a creature spell")
    void noPumpForCreatureSpell() {
        Permanent dragonauts = addDragonauts();

        harness.castFromHand(player1, new GhostWarden(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(0);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not pump when an opponent casts an instant")
    void noPumpForOpponentsInstant() {
        Permanent dragonauts = addDragonauts();

        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(0);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dragonauts = addDragonauts();

        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(2);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(0);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple spells give cumulative boosts until end of turn")
    void boostsAccumulateForMultipleSpells() {
        Permanent dragonauts = addDragonauts();

        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(4);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragonauts.getPowerModifier()).isEqualTo(0);
        assertThat(dragonauts.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each controlled copy boosts itself independently")
    void eachDragonautsBoostsItself() {
        Permanent first = addDragonauts();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WeeDragonauts());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new WeeDragonauts());

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(0);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(0);
        assertThat(opponents.getPowerModifier()).isEqualTo(0);
        assertThat(opponents.getToughnessModifier()).isEqualTo(0);
    }
}
