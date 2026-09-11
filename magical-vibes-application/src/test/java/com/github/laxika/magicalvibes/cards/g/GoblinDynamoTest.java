package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinDynamo.class, BalduvianBears.class})
class GoblinDynamoTest extends BaseCardTest {

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
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = findPermanent(player2, "Balduvian Bears");
        harness.activateAbility(player1, 0, 1, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    private Permanent addReadyDynamo(Player player) {
        return addCreatureReady(player, new GoblinDynamo());
    }
}
