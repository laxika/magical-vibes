package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarehouseTabby.class, UpTheBeanstalk.class, PlanarCleansing.class})
class WarehouseTabbyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonblocking Rat when an enchantment you control goes to the graveyard")
    void createsNonblockingRatForControlledEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));
        harness.passBothPriorities();

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
        assertThat(rat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(rat.getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment")
    void doesNotTriggerForOpponentsEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new UpTheBeanstalk());

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

    @Test
    void doesNotTriggerWhenEnchantmentIsExiled() {
        harness.addToBattlefield(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, enchantment));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void doesNotTriggerForNonenchantmentCreatureDeath() {
        harness.addToBattlefield(player1, new WarehouseTabby());
        Permanent otherTabby = harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherTabby));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void createsRatEvenIfTabbyLeavesBeforeTriggerResolves() {
        Permanent tabby = harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, tabby);
        });
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent tabby = harness.addToBattlefieldAndReturn(player1, new WarehouseTabby());
        tabby.setSummoningSick(true);
        tabby.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tabby, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void triggersForEachEnchantmentDestroyedSimultaneouslyWithTabby() {
        harness.addToBattlefield(player1, new WarehouseTabby());
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Warehouse Tabby");
        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }
}
