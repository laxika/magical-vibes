package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumengridWarden.class})
class LumengridWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Lumengrid Warden puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LumengridWarden()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Lumengrid Warden onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LumengridWarden()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lumengrid Warden");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new LumengridWarden()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Lumengrid Warden enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new LumengridWarden()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Lumengrid Warden");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Lumengrid Warden deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new LumengridWarden());
        attacker.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}

