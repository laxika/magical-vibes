package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SurgespannerTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may pay {1}{U}. If you do,
    //  return target permanent to its owner's hand."

    @Test
    @DisplayName("Paying returns a target noncreature permanent to its owner's hand")
    void payReturnsNoncreaturePermanent() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        harness.addToBattlefield(player2, new Island());
        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, islandId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying returns a target creature to its owner's hand")
    void payReturnsCreature() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining returns nothing and spends no mana")
    void declineDoesNothing() {
        Permanent surgespanner = harness.addToBattlefieldAndReturn(player1, new Surgespanner());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tap(surgespanner);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Surgespanner());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
    }
}
