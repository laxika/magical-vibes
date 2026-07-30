package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmsDealerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 4 damage to target creature, sacrificing itself as the Goblin")
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = findPermanent(player2, "Hill Giant").getId();

        harness.activateAbility(player1, 0, null, giantId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arms Dealer");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Creature with toughness greater than 4 survives the damage")
    void toughCreatureSurvives() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addToBattlefield(player2, new AvatarOfMight());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID angelId = findPermanent(player2, "Avatar of Might").getId();

        harness.activateAbility(player1, 0, null, angelId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(findPermanent(player2, "Avatar of Might").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new ArmsDealer());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
