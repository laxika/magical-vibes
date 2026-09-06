package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraconicRoar.class, GrizzlyBears.class, ShivanDragon.class})
class DraconicRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target creature without the Dragon bonus")
    void dealsDamageWithoutDragonBonus() {
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Revealing a Dragon deals 3 damage to the target creature's controller")
    void revealingDragonDealsBonusDamage() {
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        ShivanDragon revealedDragon = new ShivanDragon();
        harness.setHand(player1, List.of(new DraconicRoar(), revealedDragon));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).contains(revealedDragon);
    }

    @Test
    @DisplayName("Controlling a Dragon as cast deals 3 damage to the target creature's controller")
    void controllingDragonAsCastDealsBonusDamage() {
        ShivanDragon controlledDragon = new ShivanDragon();
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        addCreatureReady(player1, controlledDragon);
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
