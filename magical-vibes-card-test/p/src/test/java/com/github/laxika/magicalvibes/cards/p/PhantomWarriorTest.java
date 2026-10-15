package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, PhantomWarrior.class})
class PhantomWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Phantom Warrior puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new PhantomWarrior(), "{1}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Phantom Warrior onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new PhantomWarrior(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phantom Warrior");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new PhantomWarrior()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Phantom Warrior enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new PhantomWarrior(), "{1}{U}{U}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Phantom Warrior");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Phantom Warrior cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        // Player2 has Grizzly Bears as potential blocker
        addCreatureReady(player2, new GrizzlyBears());

        addCreatureReady(player1, new PhantomWarrior());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Phantom Warrior can block an opposing ground creature")
    void canBlockOpposingGroundCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PhantomWarrior());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A face-down Phantom Warrior can be blocked")
    void faceDownPhantomWarriorCanBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new PhantomWarrior());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Unblocked Phantom Warrior deals 2 damage to defending player")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new PhantomWarrior());
        declareAttackers(List.of(0));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}

