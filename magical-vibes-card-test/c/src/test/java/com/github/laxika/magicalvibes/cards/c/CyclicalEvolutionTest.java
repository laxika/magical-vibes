package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({CyclicalEvolution.class, CoalitionRelic.class, NessianCourser.class, Clockspinning.class})
class CyclicalEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Cyclical Evolution gives a target creature +3/+3 and exiles itself with three time counters")
    void castsAndExilesWithSuspendCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        CyclicalEvolution card = new CyclicalEvolution();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Cyclical Evolution's temporary boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        castNormallyOn(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cyclical Evolution cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new NessianCourser());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new CoalitionRelic());
        harness.setHand(player1, List.of(new CyclicalEvolution()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, relic.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cyclical Evolution fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        CyclicalEvolution card = new CyclicalEvolution();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    @DisplayName("A suspended Cyclical Evolution free-casts, boosts its target, and starts a new countdown")
    void suspendedCardFreeCastsAndExilesAgain() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        CyclicalEvolution card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the suspended cast leaves Cyclical Evolution exiled without time counters")
    void decliningSuspendedCastLeavesCardExiled() {
        CyclicalEvolution card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    @DisplayName("Removing the last time counter creates a separate trigger before the cast choice")
    void lastTimeCounterCreatesSeparateCastTrigger() {
        harness.addToBattlefield(player1, new NessianCourser());
        suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Clockspinning can adjust time counters after Cyclical Evolution exiles itself")
    void selfExiledCardCanBeTargetedByClockspinning() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        CyclicalEvolution card = new CyclicalEvolution();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, card.getId());
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    private void castNormallyOn(Permanent target) {
        harness.setHand(player1, List.of(new CyclicalEvolution()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private CyclicalEvolution suspendCard() {
        CyclicalEvolution card = new CyclicalEvolution();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
