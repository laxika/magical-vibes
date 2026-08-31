package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarehouseTabby.class, PhyrexianArena.class, GrizzlyBears.class})
class WarehouseTabbyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonblocking Rat when an enchantment you control goes to the graveyard")
    void createsNonblockingRatForControlledEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PhyrexianArena());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));
        harness.passBothPriorities();

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment")
    void doesNotTriggerForOpponentsEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("Activation grants deathtouch until end of turn")
    void activationGrantsDeathtouchUntilEndOfTurn() {
        Permanent tabby = addCreatureReady(player1, new WarehouseTabby());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tabby, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tabby, Keyword.DEATHTOUCH)).isFalse();
    }
}
