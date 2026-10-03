package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deprive.class, GrizzlyBears.class, Island.class})
class DepriveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as an additional cost and counters target spell")
    void returnsLandAndCountersSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, spell, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, spell.getId(), island.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Deprive");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Cannot pay the additional cost with a nonland permanent")
    void cannotReturnNonlandPermanent() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent nonland = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, spell, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, spell.getId(), nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonland);
    }

    @Test
    void cannotCastWithoutReturningLand() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Deprive");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotReturnOpponentsLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        GrizzlyBears spell = new GrizzlyBears();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, spell.getId(), island.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Island");
        harness.assertInHand(player2, "Deprive");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void returnsTappedLandToOwnerAsCostBeforeResolution() {
        Island land = new Island();
        land.setOwnerId(player1.getId());
        Permanent island = harness.addToBattlefieldAndReturn(player2, land);
        island.setTapped(true);
        GrizzlyBears spell = new GrizzlyBears();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(new Deprive()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castInstantWithSacrifice(player2, 0, spell.getId(), island.getId());

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(land);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Deprive");
    }

    @Test
    void counteringDepriveDoesNotRefundReturnedLand() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Deprive first = new Deprive();
        Deprive second = new Deprive();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, spell.getId(), firstLand.getId());
        harness.castInstantWithSacrifice(player2, 0, first.getId(), secondLand.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .contains(firstLand.getCard(), secondLand.getCard());
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
