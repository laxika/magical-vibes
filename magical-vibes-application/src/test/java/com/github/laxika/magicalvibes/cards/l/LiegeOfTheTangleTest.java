package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class LiegeOfTheTangleTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage triggers multi-permanent choice for controller's lands")
    void combatDamageTriggersLandChoice() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());
        addLand(player1, new Forest());

        resolveCombat();

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
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(forest1.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest2.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Awakened lands are 8/8 creatures")
    void awakenedLandsAre8_8Creatures() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.getCounterCount(CounterType.AWAKENING)).isEqualTo(1);
        assertThat(forest.getEffectivePower()).isEqualTo(8);
        assertThat(forest.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Awakened lands are still lands")
    void awakenedLandsAreStillLands() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("Awakening counters persist across turns (not cleared by end of turn)")
    void awakeningCountersPersistAcrossTurns() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveCombat();
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
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(forest.getCounterCount(CounterType.AWAKENING)).isZero();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses not to put"));
    }

    @Test
    @DisplayName("No trigger when Liege is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());

        // Add blocker that can survive (8/8 needed to fully block, but any creature blocks)
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when controller has no lands")
    void noTriggerWhenNoLands() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        // player1 has no lands

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("controls no lands"));
    }

    @Test
    @DisplayName("Can choose only some lands, not all")
    void canChooseSubsetOfLands() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());

        resolveCombat();

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
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        addLand(player1, new Forest());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Game advances to postcombat main after choice")
    void gameAdvancesAfterChoice() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Awakened lands from different types all become 8/8")
    void differentLandTypesAllBecome8_8() {
        Permanent liege = addReadyCreature(player1, new LiegeOfTheTangle());
        liege.setAttacking(true);
        Permanent forest = addLand(player1, new Forest());
        Permanent mountain = addLand(player1, new Mountain());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), mountain.getId()));

        assertThat(forest.getEffectivePower()).isEqualTo(8);
        assertThat(forest.getEffectiveToughness()).isEqualTo(8);
        assertThat(mountain.getEffectivePower()).isEqualTo(8);
        assertThat(mountain.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
    }
}
