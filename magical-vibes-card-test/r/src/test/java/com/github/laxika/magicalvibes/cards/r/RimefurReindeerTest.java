package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimefurReindeer.class, GloriousAnthem.class, GrizzlyBears.class})
class RimefurReindeerTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control taps a target creature an opponent controls")
    void allyEnchantmentEntryTapsTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RimefurReindeer());

        castGloriousAnthem();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-enchantment entering under your control does not trigger")
    void nonEnchantmentEntryDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RimefurReindeer());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RimefurReindeer());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RimefurReindeer());

        castGloriousAnthem();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped opponent creature is a legal target")
    void alreadyTappedCreatureIsLegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.addToBattlefield(player1, new RimefurReindeer());

        castGloriousAnthem();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment entering with no legal targets does not leave a pending choice")
    void noOpponentCreaturesLeavesNoPendingChoice() {
        Permanent reindeer = harness.addToBattlefieldAndReturn(player1, new RimefurReindeer());
        harness.addToBattlefield(player2, new GloriousAnthem());

        castGloriousAnthem();
        harness.passBothPriorities();

        assertThat(reindeer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves after Rimefur Reindeer leaves the battlefield")
    void triggerResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent reindeer = harness.addToBattlefieldAndReturn(player1, new RimefurReindeer());

        castGloriousAnthem();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        reindeer.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Rimefur Reindeer");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castGloriousAnthem() {
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
    }
}
