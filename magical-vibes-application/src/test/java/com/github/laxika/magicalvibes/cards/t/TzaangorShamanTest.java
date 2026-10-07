package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.s.Starstorm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TzaangorShaman.class, LightningBolt.class, BlasphemousAct.class, Starstorm.class})
class TzaangorShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the next instant or sorcery copy")
    void combatDamageCopiesNextInstantOrSorcery() {
        Permanent shaman = addCreatureReady(player1, new TzaangorShaman());
        shaman.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gameData.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Without combat damage, the ability does not set up a copy")
    void withoutCombatDamageDoesNotSetUpCopy() {
        addCreatureReady(player1, new TzaangorShaman());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    void copyResolvesWithOriginalTargetsAndOnlyNextSpellIsCopied() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.assertLife(player2, 17);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 11);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseNewTargetWithoutChangingOriginal() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent otherShaman = addCreatureReady(player2, new TzaangorShaman());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherShaman.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Tzaangor Shaman");
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleShamansEachCopyTheNextSpell() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.assertLife(player2, 14);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingCreatureDoesNotConsumeDelayedCopy() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new TzaangorShaman(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Tzaangor Shaman")).isEqualTo(2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 11);
    }

    @Test
    void untargetedCopyPreservesXAndSurvivesSourceLeaving() {
        Permanent shaman = addCreatureReady(player1, new TzaangorShaman());
        shaman.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> assertThat(entry.getXValue()).isEqualTo(2));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tzaangor Shaman");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceryWithoutTargetsIsCopied() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL));
        assertThat(gd.stack).anyMatch(entry -> entry.isCopy());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Tzaangor Shaman");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellCastBeforeCombatDamageTriggerResolvesIsNotCopied() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 8);
    }

    @Test
    void opponentsSpellDoesNotConsumeDelayedCopy() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 11);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        addCreatureReady(player1, new TzaangorShaman()).setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }
}
