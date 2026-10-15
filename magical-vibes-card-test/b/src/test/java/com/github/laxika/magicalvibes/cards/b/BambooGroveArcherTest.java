package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BambooGroveArcher.class, AirElemental.class, GrizzlyBears.class})
class BambooGroveArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Bamboo Grove Archer from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new BambooGroveArcher());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Reach lets Bamboo Grove Archer block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent archer = addCreatureReady(player2, new BambooGroveArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Channel destroys target creature with flying and discards Bamboo Grove Archer")
    void channelDestroysFlyingCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Bamboo Grove Archer");
    }

    @Test
    @DisplayName("Channel cannot target a creature without flying")
    void channelRejectsNonFlyingCreature() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bamboo Grove Archer");
    }

    @Test
    @DisplayName("Channel discards its source immediately and may destroy your own flying creature")
    void channelCanTargetOwnCreatureAndPaysDiscardBeforeResolution() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Bamboo Grove Archer");
        harness.assertInGraveyard(player1, "Bamboo Grove Archer");
        harness.assertOnBattlefield(player1, "Air Elemental");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Channel requires the full five mana")
    void channelRejectsInsufficientMana() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bamboo Grove Archer");
        harness.assertNotInGraveyard(player1, "Bamboo Grove Archer");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Channel requires green mana")
    void channelRejectsMissingGreenMana() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bamboo Grove Archer");
        harness.assertNotInGraveyard(player1, "Bamboo Grove Archer");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Channel does not return its discarded source when the target leaves before resolution")
    void channelWithRemovedTargetDoesNotRefundDiscard() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        harness.setHand(player2, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.activateHandAbility(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Bamboo Grove Archer");
        harness.assertInGraveyard(player1, "Bamboo Grove Archer");
        harness.assertNotInHand(player2, "Bamboo Grove Archer");
        harness.assertInGraveyard(player2, "Bamboo Grove Archer");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }
}
