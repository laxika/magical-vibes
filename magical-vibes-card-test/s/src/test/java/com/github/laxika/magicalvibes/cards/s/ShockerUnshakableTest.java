package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShockerUnshakable.class, GrizzlyBears.class})
class ShockerUnshakableTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to target creature and its controller")
    void etbDealsDamageToCreatureAndController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllerTurn() {
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new ShockerUnshakable());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during an opponent's turn")
    void doesNotHaveFirstStrikeDuringOpponentsTurn() {
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new ShockerUnshakable());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike updates as the active player changes")
    void firstStrikeUpdatesWithActivePlayer() {
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new ShockerUnshakable());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isTrue();
        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isFalse();
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Targeting your own creature damages you rather than the opponent")
    void ownCreatureControllerTakesDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A surviving target receives exactly two marked damage")
    void survivingTargetReceivesTwoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShockerUnshakable());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shocker, Unshakable");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An illegal creature target prevents damage to its controller too")
    void removedTargetPreventsAllDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability still deals damage after Shocker leaves the battlefield")
    void removedSourceStillDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent source = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Shocker, Unshakable"));
        source.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Shocker, Unshakable");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }
}
