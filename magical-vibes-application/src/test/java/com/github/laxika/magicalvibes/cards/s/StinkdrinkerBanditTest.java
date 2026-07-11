package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StinkdrinkerBanditTest extends BaseCardTest {

    // ===== "Whenever a Rogue you control attacks and isn't blocked, it gets +2/+1 until end of turn" =====

    @Test
    @DisplayName("Unblocked Rogue gets +2/+1 until end of turn")
    void unblockedRogueGetsBoost() {
        Permanent bandit = addReady(player1, new StinkdrinkerBandit());
        bandit.setAttacking(true);
        addReady(player2, new GrizzlyBears()); // a potential blocker that declines to block

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // no blocks — the Bandit is unblocked
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(2);
        assertThat(bandit.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked Rogue does not get the boost")
    void blockedRogueGetsNoBoost() {
        Permanent bandit = addReady(player1, new StinkdrinkerBandit());
        bandit.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(0);
        assertThat(bandit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A non-Rogue attacking unblocked is not boosted")
    void nonRogueUnblockedNotBoosted() {
        addReady(player1, new StinkdrinkerBandit()); // source, not attacking
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +2/+1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bandit = addReady(player1, new StinkdrinkerBandit());
        bandit.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(0);
        assertThat(bandit.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Prowl {1}{B} (Goblin or Rogue) =====

    @Test
    @DisplayName("Can be cast for its prowl cost after Goblin combat damage")
    void prowlAvailableAfterGoblinDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new StinkdrinkerBandit()));
        harness.addMana(player1, ManaColor.BLACK, 2); // prowl {1}{B}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stinkdrinker Bandit"));
    }

    @Test
    @DisplayName("Prowl is unavailable without qualifying combat damage")
    void prowlUnavailableWithoutQualifyingDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new StinkdrinkerBandit()));
        harness.addMana(player1, ManaColor.BLACK, 2); // enough for prowl {1}{B}, not for the normal {3}{B}

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
