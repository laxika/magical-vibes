package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HollowhengeBeast;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyAllure.class, DawntreaderElk.class, HollowhengeBeast.class})
class DeadlyAllureTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Deadly Allure targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Deadly Allure");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving grants deathtouch and sets must-be-blocked flag")
    void resolvingGrantsDeathtouchAndMustBeBlocked() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Deadly Allure goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deadly Allure");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Deadly Allure");
    }

    @Test
    @DisplayName("Flashback from graveyard grants deathtouch and must-be-blocked")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    void cannotAvoidRequirementByBlockingAnotherAttacker() {
        Permanent target = addCreatureReady(player1, new DawntreaderElk());
        addCreatureReady(player1, new HollowhengeBeast());
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresOneBlockerButDoesNotRequireAllBlockers() {
        Permanent target = addCreatureReady(player1, new DawntreaderElk());
        Permanent blocker = addCreatureReady(player2, new DawntreaderElk());
        Permanent otherBlocker = addCreatureReady(player2, new HollowhengeBeast());
        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isFalse();
    }

    @Test
    void canRemainUnblockedWhenOnlyBlockerIsTapped() {
        Permanent target = addCreatureReady(player1, new DawntreaderElk());
        Permanent blocker = addCreatureReady(player2, new HollowhengeBeast());
        blocker.tap();
        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void deathtouchKillsBlockerWithGreaterToughness() {
        Permanent target = addCreatureReady(player1, new DawntreaderElk());
        harness.addToBattlefield(player2, new HollowhengeBeast());
        harness.setHand(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Hollowhenge Beast");
        harness.assertInGraveyard(player1, "Dawntreader Elk");
    }

    @Test
    void flashbackExilesEvenWhenTargetDisappears() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setGraveyard(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFlashback(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Deadly Allure");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deadly Allure"));
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new DeadlyAllure()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotInGraveyard(player1, "Deadly Allure");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deadly Allure"));
    }
}
