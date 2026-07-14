package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirrorSheenTest extends BaseCardTest {

    private int prepareSheen() {
        Permanent sheen = new Permanent(new MirrorSheen());
        gd.playerBattlefields.get(player2.getId()).add(sheen);
        harness.addMana(player2, ManaColor.BLUE, 3);
        return gd.playerBattlefields.get(player2.getId()).indexOf(sheen);
    }

    @Test
    @DisplayName("Copies an instant spell that targets you")
    void copiesInstantTargetingYou() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player2.getId());
        assertThat(copy.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The copy resolves, dealing the spell's damage a second time")
    void copyDealsDamageAgain() {
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();                    // ability resolves -> copy created
        harness.handleMayAbilityChosen(player2, false);  // keep the copy's target (player2)
        harness.passBothPriorities();                    // copy resolves -> 2 damage
        harness.passBothPriorities();                    // original Shock resolves -> 2 damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot copy a spell that targets an opponent instead of you")
    void cannotCopySpellTargetingOpponent() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, sheenIdx, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
