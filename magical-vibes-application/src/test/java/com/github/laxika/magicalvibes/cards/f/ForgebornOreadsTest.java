package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgebornOreads.class, FontOfFertility.class, GoldenHind.class, Hubris.class})
class ForgebornOreadsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry deals 1 damage to a target player")
    void ownEntryDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        castForgebornOreads();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Its own entry deals 1 damage to a target creature")
    void ownEntryDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GoldenHind());
        castForgebornOreads();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Golden Hind"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Golden Hind");
        harness.assertInGraveyard(player2, "Golden Hind");
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void anotherEnchantmentEntryTriggers() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ForgebornOreads());
        harness.castFromHand(player1, new FontOfFertility(), "{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-enchantment entering under your control does not trigger it")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ForgebornOreads());
        harness.castFromHand(player1, new GoldenHind(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ForgebornOreads());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FontOfFertility(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondOreadsTriggersBothExactlyOnce() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ForgebornOreads());
        castForgebornOreads();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enchantmentEnteringWithoutBeingCastTriggers() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ForgebornOreads());

        harness.enterBattlefieldAndReturn(player1, new FontOfFertility());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterSourceLeaves() {
        harness.setLife(player2, 20);
        var oreads = harness.addToBattlefieldAndReturn(player1, new ForgebornOreads());
        harness.castFromHand(player1, new FontOfFertility(), "{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, oreads.getId());
        harness.assertNotOnBattlefield(player1, "Forgeborn Oreads");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageDoesNotFollowTargetReturnedToHand() {
        var target = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        castForgebornOreads();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Golden Hind");
        harness.assertNotInGraveyard(player2, "Golden Hind");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        castForgebornOreads();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    private void castForgebornOreads() {
        harness.castFromHand(player1, new ForgebornOreads(), "{2}{R}{R}");
    }
}
