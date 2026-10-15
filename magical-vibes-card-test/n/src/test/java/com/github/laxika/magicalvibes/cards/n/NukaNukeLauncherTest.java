package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NukaNukeLauncher.class, GrizzlyBears.class, RagingGoblin.class, Shock.class, AjaniGoldmane.class})
class NukaNukeLauncherTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 and intimidate")
    void equippedCreatureGetsBoostAndIntimidate() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        launcher.setAttachedTo(creature.getId());
        addCreatureReady(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    @DisplayName("Attacking makes the defending player's spells give them two rad counters")
    void defendingPlayerGetsRadCountersWhenCastingSpells() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        launcher.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerRadCounters).doesNotContainKey(player1.getId());
        assertThat(gd.playerRadCounters).doesNotContainKey(player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Spell trigger lasts through the defending player's next turn")
    void spellTriggerExpiresAfterDefendingPlayersNextTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        launcher.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        endTurn();

        castSpellAs(player2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);

        endTurn();
        endTurn();
        castSpellAs(player2);

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void equipAttachesToOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(launcher.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    void registeredTriggerSurvivesEquipmentLeavingAndTriggersForEverySpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        launcher.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(launcher);
        gd.playerGraveyards.get(player1.getId()).add(launcher.getCard());

        castSpellAs(player2);
        castSpellAs(player2);

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void defendingPlayerStillGetsRadCountersIfAttackedPlaneswalkerDiesBeforeTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new NukaNukeLauncher());
        launcher.setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.castAndResolveInstant(player1, 0, planeswalker.getId());
        harness.assertNotOnBattlefield(player2, "Ajani Goldmane");
        resolveAllTriggers();

        castSpellAs(player2);

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    private void castSpellAs(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player, 0, player == player1 ? player2.getId() : player1.getId());
    }

    private void endTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
