package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastNightTogether.class, GrizzlyBears.class, HillGiant.class})
class LastNightTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, strengthens, and grants keywords to both chosen creatures")
    void buffsBothChosenCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        bears.tap();
        giant.tap();

        castLastNightTogether(bears, giant);

        for (Permanent creature : List.of(bears, giant)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("The added combat allows only the chosen creatures to attack")
    void restrictsTheAdditionalCombatToChosenCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castLastNightTogether(bears, giant);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        PendingInteraction.AttackerDeclaration prompt = gd.interaction.activeInteraction(
                PendingInteraction.AttackerDeclaration.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.attackerIndices()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(giant));
        assertThat(prompt.attackerIndices()).doesNotContain(
                gd.playerBattlefields.get(player1.getId()).indexOf(unchosen));
    }

    private void castLastNightTogether(Permanent first, Permanent second) {
        putLastNightTogetherOnStack(first, second);
        harness.passBothPriorities();
    }

    private void putLastNightTogetherOnStack(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new LastNightTogether()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice")
    void requiresTwoDistinctCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> putLastNightTogetherOnStack(creature, creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A surviving target is enhanced and an additional combat is created")
    void resolvesWithOneTargetRemaining() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        second.tap();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        putLastNightTogetherOnStack(first, second);
        gd.playerBattlefields.get(player1.getId()).remove(first);

        harness.passBothPriorities();

        assertThat(second.isTapped()).isFalse();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        PendingInteraction.AttackerDeclaration prompt = gd.interaction.activeInteraction(
                PendingInteraction.AttackerDeclaration.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.attackerIndices()).containsExactly(
                gd.playerBattlefields.get(player1.getId()).indexOf(second));
    }

    @Test
    @DisplayName("No additional combat is created when both targets leave before resolution")
    void doesNotResolveWithNoLegalTargets() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        putLastNightTogetherOnStack(first, second);
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(first, second));

        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chosen summoning-sick creatures can attack without tapping")
    void hasteAndVigilanceAllowUntappedAttacks() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castLastNightTogether(first, second);
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            gs.declareAttackers(gd, player1, List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(first),
                    gd.playerBattlefields.get(player1.getId()).indexOf(second)));
            resolveCombat();
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("An added combat after the first main leads directly into the normal combat")
    void noMainPhaseBetweenAddedAndNormalCombat() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castLastNightTogether(first, second);

        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> harness.passUntilWithNoAttackers(player1, TurnStep.END_OF_COMBAT));
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("An added combat after the second main leads directly into the ending phase")
    void noMainPhaseAfterAddedPostcombatCombat() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castLastNightTogether(first, second);

        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> harness.passUntilWithNoAttackers(player1, TurnStep.END_OF_COMBAT));
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Creatures controlled by an opponent can be chosen and receive all benefits")
    void canChooseOpponentsCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        first.tap();
        second.tap();

        castLastNightTogether(first, second);

        for (Permanent creature : List.of(first, second)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("The granted keywords expire at cleanup but the counters remain")
    void keywordsExpireButCountersRemain() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new HillGiant());
        castLastNightTogether(first, second);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        for (Permanent creature : List.of(first, second)) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        }
    }
}
