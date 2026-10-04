package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvilEyeOfUrborg.class, AshcoatBear.class, HavenwoodWurm.class, SuddenDeath.class})
class EvilEyeOfUrborgTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Eye creature you control cannot attack")
    void nonEyeCreatureCannotAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfUrborg());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        int bearIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bear);
        assertThatThrownBy(() -> declareAttackers(List.of(bearIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Eye creature you control can attack")
    void eyeCreatureCanAttack() {
        addCreatureReady(player1, new EvilEyeOfUrborg());
        harness.setLife(player2, 20);

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The restriction does not affect non-Eye creatures controlled by an opponent")
    void opponentNonEyeCreatureCanAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfUrborg());
        Permanent bear = addCreatureReady(player2, new AshcoatBear());

        int bearIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bear);
        declareAttackers(player2, List.of(bearIndex));

        assertThat(bear.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("When Evil Eye becomes blocked, it destroys the blocker immediately when the trigger resolves")
    void becomesBlockedDestroysBlocker() {
        addCreatureReady(player1, new EvilEyeOfUrborg());
        Permanent blocker = addCreatureReady(player2, new HavenwoodWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Havenwood Wurm");
        harness.assertInGraveyard(player2, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Each creature blocking Evil Eye is destroyed by its own trigger")
    void eachBlockerIsDestroyed() {
        addCreatureReady(player1, new EvilEyeOfUrborg());
        Permanent firstBlocker = addCreatureReady(player2, new HavenwoodWurm());
        Permanent secondBlocker = addCreatureReady(player2, new HavenwoodWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Havenwood Wurm", "Havenwood Wurm");
    }

    @Test
    @DisplayName("The destruction trigger resolves even if Evil Eye leaves the battlefield")
    void destroysBlockerAfterSourceLeavesBattlefield() {
        Permanent eye = addCreatureReady(player1, new EvilEyeOfUrborg());
        addCreatureReady(player2, new HavenwoodWurm());
        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player2, 0, eye.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Evil Eye of Urborg");
        harness.assertOnBattlefield(player2, "Havenwood Wurm");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Havenwood Wurm");
        harness.assertInGraveyard(player2, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Blocking with Evil Eye does not trigger its destruction ability")
    void blockingDoesNotDestroyAttackerBeforeCombatDamage() {
        addCreatureReady(player1, new HavenwoodWurm());
        addCreatureReady(player2, new EvilEyeOfUrborg());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Havenwood Wurm");
        harness.assertOnBattlefield(player2, "Evil Eye of Urborg");
    }
}
