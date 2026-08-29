package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhitewaterNaiadsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry makes a target creature unblockable")
    void ownEntryMakesTargetUnblockable() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWhitewaterNaiads(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void allyEnchantmentEntryMakesTargetUnblockable() {
        harness.addToBattlefield(player1, new WhitewaterNaiads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new WhitewaterNaiads());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The effect wears off at the end of the turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWhitewaterNaiads(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Its entry cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new WhitewaterNaiads()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWhitewaterNaiads(com.github.laxika.magicalvibes.model.Player player,
                                      java.util.UUID targetId) {
        harness.setHand(player, List.of(new WhitewaterNaiads()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player, 0, targetId);
    }
}
