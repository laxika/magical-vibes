package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDynamo.class, AvenEnvoy.class})
class GoblinDynamoTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to a creature")
    void tapAbilityDealsDamageToCreature() {
        addReadyDynamo(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to a player")
    void tapAbilityDealsDamageToPlayer() {
        Permanent dynamo = addReadyDynamo(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(dynamo.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrifice ability deals X damage and sacrifices Goblin Dynamo")
    void sacrificeAbilityDealsXDamage() {
        addReadyDynamo(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 3, player2.getId());
        harness.assertInGraveyard(player1, "Goblin Dynamo");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Sacrifice ability can deal X damage to a creature")
    void sacrificeAbilityDealsDamageToCreature() {
        addReadyDynamo(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Envoy");
    }

    @Test
    @DisplayName("Zero X still costs red mana and sacrifices Dynamo without dealing damage")
    void sacrificeAbilityAllowsZeroX() {
        addReadyDynamo(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, 0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Dynamo");
        harness.assertInGraveyard(player1, "Goblin Dynamo");
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dynamo can target itself with its tap ability")
    void tapAbilityCanTargetItself() {
        Permanent dynamo = addReadyDynamo(player1);

        harness.activateAbility(player1, 0, null, dynamo.getId());
        harness.passBothPriorities();

        assertThat(dynamo.isTapped()).isTrue();
        assertThat(dynamo.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Goblin Dynamo");
    }

    @Test
    @DisplayName("Self-targeted sacrifice ability loses its target after paying the sacrifice cost")
    void sacrificeAbilityCanTargetItself() {
        Permanent dynamo = addReadyDynamo(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, 4, dynamo.getId());

        harness.assertInGraveyard(player1, "Goblin Dynamo");
        harness.assertNotOnBattlefield(player1, "Goblin Dynamo");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both tap costs are prevented by summoning sickness")
    void summoningSicknessPreventsBothAbilities() {
        harness.addToBattlefield(player1, new GoblinDynamo());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Dynamo");
        harness.assertNotInGraveyard(player1, "Goblin Dynamo");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Dynamo cannot activate either ability")
    void tappedDynamoCannotActivateEitherAbility() {
        Permanent dynamo = addReadyDynamo(player1);
        dynamo.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Dynamo");
        harness.assertNotInGraveyard(player1, "Goblin Dynamo");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability requires red mana even when X is zero")
    void sacrificeAbilityRequiresRedMana() {
        Permanent dynamo = addReadyDynamo(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dynamo.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Goblin Dynamo");
        harness.assertNotInGraveyard(player1, "Goblin Dynamo");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDynamo(Player player) {
        return addCreatureReady(player, new GoblinDynamo());
    }
}
