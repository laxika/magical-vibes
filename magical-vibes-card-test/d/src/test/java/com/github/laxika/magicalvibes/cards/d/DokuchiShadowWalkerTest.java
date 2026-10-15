package com.github.laxika.magicalvibes.cards.d;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DokuchiShadowWalker.class})
class DokuchiShadowWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Dokuchi Shadow-Walker in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new DokuchiShadowWalker());
        addCreatureReady(player2, new DokuchiShadowWalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.setHand(player1, List.of(new DokuchiShadowWalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.clearPriorityPassed();
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dokuchi Shadow-Walker");
        Permanent shadowWalker = findPermanent(player1, "Dokuchi Shadow-Walker");
        assertThat(shadowWalker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new DokuchiShadowWalker());
        addCreatureReady(player2, new DokuchiShadowWalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DokuchiShadowWalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    @Test
    @DisplayName("Returning the attacker is a cost paid before ninjutsu resolves")
    void returnsAttackerBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new DokuchiShadowWalker());
        addCreatureReady(player2, new DokuchiShadowWalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        DokuchiShadowWalker ninja = new DokuchiShadowWalker();
        harness.setHand(player1, List.of(ninja));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(ninja, attacker.getCard());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dokuchi Shadow-Walker").getCard()).isSameAs(ninja);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(attacker.getCard());
    }

    @Test
    @DisplayName("Insufficient ninjutsu mana leaves the attacker and ninja in their original zones")
    void insufficientManaDoesNotReturnAttacker() {
        Permanent attacker = addCreatureReady(player1, new DokuchiShadowWalker());
        addCreatureReady(player2, new DokuchiShadowWalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        DokuchiShadowWalker ninja = new DokuchiShadowWalker();
        harness.setHand(player1, List.of(ninja));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ninja);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu cannot return an attacker before blockers are declared")
    void cannotActivateBeforeBlockers() {
        Permanent attacker = addCreatureReady(player1, new DokuchiShadowWalker());
        addCreatureReady(player2, new DokuchiShadowWalker());
        harness.setHand(player1, List.of(new DokuchiShadowWalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }
}
