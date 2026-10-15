package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AlaniaDivergentStorm;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.e.ExperimentOne;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OtterballAntics.class, AlaniaDivergentStorm.class, BarkformHarvester.class,
        ExperimentOne.class, MightOfTheMeek.class})
class OtterballAnticsTest extends BaseCardTest {

    @Test
    @DisplayName("A hand cast creates a 1/1 Otter without a counter")
    void handCastCreatesOtterWithoutCounter() {
        harness.setHand(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent otter = otterToken();
        assertThat(otter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("A flashback cast creates an Otter with a +1/+1 counter")
    void flashbackCastCreatesOtterWithCounter() {
        harness.setGraveyard(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        Permanent otter = otterToken();
        assertThat(otter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Otter's prowess triggers for a noncreature spell")
    void otterHasProwess() {
        harness.setHand(player1, List.of(new OtterballAntics(), new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent otter = otterToken();

        harness.castInstant(player1, 0, otter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell copy creates an Otter without a counter")
    void copiedSpellCreatesOtterWithoutCounter() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setHand(player1, List.of(new OtterballAntics()));
        harness.setLibrary(player2, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        Permanent copyOtter = otterToken();
        assertThat(copyOtter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copyOtter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copyOtter)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The flashback Otter enters as a 1/1 before receiving its counter")
    void counterIsPlacedAfterEntering() {
        Permanent experiment = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        harness.setGraveyard(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(otterToken().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature spell does not trigger the Otter's prowess")
    void creatureSpellDoesNotTriggerProwess() {
        harness.setHand(player1, List.of(new OtterballAntics(), new BarkformHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent otter = otterToken();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger the Otter's prowess")
    void opponentSpellDoesNotTriggerProwess() {
        harness.setHand(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent otter = otterToken();
        harness.setHand(player2, List.of(new MightOfTheMeek()));
        harness.setLibrary(player2, List.of(new BarkformHarvester()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, otter.getId());

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prowess boosts accumulate and expire at end of turn")
    void prowessBoostsAccumulateAndExpire() {
        harness.setHand(player1, List.of(new OtterballAntics(), new OtterballAntics(), new OtterballAntics()));
        harness.setLibrary(player2, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent otter = otterToken();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("A hand-cast card can be flashed back and is then exiled")
    void flashbackExilesCardAfterHandCast() {
        OtterballAntics spell = new OtterballAntics();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent otterToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
