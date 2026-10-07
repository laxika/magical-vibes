package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFallOfKroog.class, Forest.class, Mountain.class, GiantSpider.class, YotianFrontliner.class})
class TheFallOfKroogTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the chosen opponent's land and damages that player and their creatures")
    void resolvesAgainstChosenOpponent() {
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), land.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires the land to be controlled by the chosen opponent")
    void rejectsLandControlledByAnotherPlayer() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player2.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires targeting an opponent")
    void rejectsControllerAsOpponentTarget() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player1.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still damages the opponent and their creatures when the targeted land leaves")
    void dealsDamageWhenLandTargetLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), land.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "The Fall of Kroog");
    }

    @Test
    @DisplayName("A land that changes controllers survives while the opponent still takes damage")
    void doesNotDestroyLandThatChangedControllers() {
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), land.getId()));
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player2.getId()).remove(land);
            gd.playerBattlefields.get(player1.getId()).add(land);
        });
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals lethal damage to every opposing one-toughness creature and spares other permanents")
    void damagesEveryOpposingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new YotianFrontliner());
        harness.addToBattlefield(player2, new YotianFrontliner());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), land.getId()));

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Yotian Frontliner");
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card instanceof YotianFrontliner)).hasSize(2);
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Yotian Frontliner");
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }
}
