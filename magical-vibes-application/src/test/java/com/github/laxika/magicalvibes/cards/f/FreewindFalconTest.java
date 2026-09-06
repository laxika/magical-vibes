package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.v.ViashivanDragon;
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

@CardUsed({FreewindFalcon.class, ViashivanDragon.class, Fireblast.class, FuneralCharm.class})
class FreewindFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Red flyer cannot block Freewind Falcon")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new FreewindFalcon());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ViashivanDragon());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent falcon = addCreatureReady(player2, new FreewindFalcon());

        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent falcon = addCreatureReady(player1, new FreewindFalcon());

        harness.setHand(player1, List.of(new FuneralCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstant(player1, 0, 1, List.of(falcon.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Funeral Charm");
    }

    @Test
    @DisplayName("Red combat damage to Freewind Falcon is prevented")
    void redCombatDamageIsPrevented() {
        Permanent dragon = addCreatureReady(player1, new ViashivanDragon());
        dragon.setAttacking(true);
        Permanent falcon = addCreatureReady(player2, new FreewindFalcon());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(falcon.getMarkedDamage()).isZero();
        assertThat(dragon.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Viashivan Dragon");
        harness.assertOnBattlefield(player2, "Freewind Falcon");
    }

    @Test
    @CardUsed(FireWhip.class)
    @DisplayName("Cannot be enchanted by red Aura")
    void cannotBeEnchantedByRedAura() {
        Permanent falcon = addCreatureReady(player1, new FreewindFalcon());

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
