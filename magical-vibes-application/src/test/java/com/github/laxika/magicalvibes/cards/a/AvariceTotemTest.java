package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvariceTotem.class, Arachnoid.class, Forest.class})
class AvariceTotemTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Exchanges control of itself and the target nonland permanent")
    void exchangesControl() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avarice Totem");
        harness.assertNotOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player1, "Arachnoid");
        harness.assertNotOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Can target a nonland permanent controlled by the same player")
    void canTargetOwnNonlandPermanent() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player1, "Arachnoid");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
        harness.assertNotOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
