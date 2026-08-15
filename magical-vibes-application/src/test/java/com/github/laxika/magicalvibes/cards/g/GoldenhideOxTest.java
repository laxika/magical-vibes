package com.github.laxika.magicalvibes.cards.g;

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

class GoldenhideOxTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry makes a target creature must be blocked")
    void ownEntryMakesTargetMustBeBlocked() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGoldenhideOx(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void allyEnchantmentEntryMakesTargetMustBeBlocked() {
        harness.addToBattlefield(player1, new GoldenhideOx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoldenhideOx());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The effect wears off at the end of the turn")
    void mustBeBlockedWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGoldenhideOx(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Its entry cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new GoldenhideOx()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGoldenhideOx(com.github.laxika.magicalvibes.model.Player player,
                                  java.util.UUID targetId) {
        harness.setHand(player, List.of(new GoldenhideOx()));
        harness.addMana(player, ManaColor.GREEN, 6);
        harness.castCreature(player, 0, 0, targetId);
    }
}
