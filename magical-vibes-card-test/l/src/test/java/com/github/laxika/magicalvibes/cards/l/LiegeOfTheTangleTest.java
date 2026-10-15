package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({LiegeOfTheTangle.class, Forest.class, Mountain.class})
class LiegeOfTheTangleTest extends BaseCardTest {

    private Permanent addLand(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void resolveLiegeCombat() {
        resolveCombat();
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Dealing combat damage triggers multi-permanent choice for controller's lands")
    void combatDamageTriggersLandChoice() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());
        addLand(player1, new Forest());

        resolveLiegeCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).context())
                .isInstanceOf(MultiPermanentChoiceContext.AwakeningCounterPlacement.class);
    }

    @Test
    @DisplayName("Choosing lands puts awakening counters on them")
    void choosingLandsPutsAwakeningCounters() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(forest1.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest2.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Awakened lands are 8/8 creatures")
    void awakenedLandsAre8_8Creatures() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveLiegeCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest.getEffectivePower()).isEqualTo(8);
        assertThat(forest.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Awakened lands are still lands")
    void awakenedLandsAreStillLands() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveLiegeCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gqs.getEffectiveCardTypes(gd, forest)).contains(CardType.LAND, CardType.CREATURE);
    }

    @Test
    @DisplayName("Awakening counters persist across turns (not cleared by end of turn)")
    void awakeningCountersPersistAcrossTurns() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveLiegeCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        // Simulate end of turn reset
        forest.resetModifiers();

        assertThat(forest.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest.getEffectivePower()).isEqualTo(8);
        assertThat(forest.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Choosing no lands is allowed")
    void choosingNoLandsIsAllowed() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(forest.getCounterCount(CounterType.AWAKENING)).isZero();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses not to put"));
    }

    @Test
    @DisplayName("No trigger when Liege is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());

        Permanent blocker = addCreatureReady(player2, new LiegeOfTheTangle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveLiegeCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when controller has no lands")
    void noTriggerWhenNoLands() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        // player1 has no lands

        resolveLiegeCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("controls no lands"));
    }

    @Test
    @DisplayName("Can choose only some lands, not all")
    void canChooseSubsetOfLands() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest1.getId()));

        assertThat(forest1.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest2.getCounterCount(CounterType.AWAKENING)).isZero();
        assertThat(gqs.isCreature(gd, forest1)).isTrue();
        assertThat(gqs.isCreature(gd, forest2)).isFalse();
    }

    @Test
    @DisplayName("Defender takes combat damage from Liege")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Game advances to postcombat main after choice")
    void gameAdvancesAfterChoice() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Awakened lands from different types all become 8/8")
    void differentLandTypesAllBecome8_8() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());
        Permanent mountain = addLand(player1, new Mountain());

        resolveLiegeCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), mountain.getId()));

        assertThat(forest.getEffectivePower()).isEqualTo(8);
        assertThat(forest.getEffectiveToughness()).isEqualTo(8);
        assertThat(mountain.getEffectivePower()).isEqualTo(8);
        assertThat(mountain.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
    }

    @Test
    void awakenedLandIsGreenElementalAfterLiegeLeaves() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent mountain = addLand(player1, new Mountain());

        resolveLiegeCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(liege);

        assertThat(gqs.getEffectiveColors(gd, mountain)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, mountain)).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(8);
        assertThat(gqs.getEffectiveCardTypes(gd, mountain)).contains(CardType.LAND, CardType.CREATURE);
    }

    @Test
    @CardUsed({DryadArbor.class})
    void existingLandCreatureBecomesEightEightElemental() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent arbor = addLand(player1, new DryadArbor());

        resolveLiegeCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(arbor.getId()));

        assertThat(arbor.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, arbor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, arbor)).isEqualTo(8);
        assertThat(gqs.effectiveCreatureSubtypes(gd, arbor)).containsExactly(CardSubtype.ELEMENTAL);
    }

    @Test
    void opponentsLandsAreNotOfferedForAwakening() {
        Permanent liege = addCreatureReady(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent ownLand = addLand(player1, new Forest());
        Permanent opposingLand = addLand(player2, new Forest());

        resolveLiegeCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId()));

        assertThat(ownLand.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(opposingLand.getCounterCount(CounterType.AWAKENING)).isZero();
        assertThat(gqs.isCreature(gd, opposingLand)).isFalse();
    }
}
