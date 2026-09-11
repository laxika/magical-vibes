package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenImpact.class, Plains.class, Island.class, WindDrake.class})
class SuddenImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sudden Impact targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Sudden Impact deals damage equal to target player's hand size")
    void dealsDamageEqualToHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of(new Plains(), new Island(), new WindDrake()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Sudden Impact uses target player's hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of(new Plains(), new Island()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new WindDrake());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Sudden Impact deals 0 damage if target player has no cards in hand")
    void dealsZeroDamageWithEmptyHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sudden Impact can target its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SuddenImpact(), new Plains(), new Island()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sudden Impact cannot target a creature")
    void cannotTargetCreature() {
        Permanent drake = new Permanent(new WindDrake());
        gd.playerBattlefields.get(player2.getId()).add(drake);

        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, drake.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

