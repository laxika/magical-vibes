package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullCatapult.class, BalduvianBears.class})
class SkullCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player; taps and sacrifices a creature as cost")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new BalduvianBears()); // sacrifice fodder
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2")
    void deals2DamageKillingCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new BalduvianBears()); // sacrifice fodder
        harness.addToBattlefield(player2, new BalduvianBears()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Balduvian Bears");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new BalduvianBears());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Balduvian Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
