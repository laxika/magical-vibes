package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BullElephant;
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

@CardUsed({FreewindFalcon.class, ViashivanDragon.class, Fireblast.class, FuneralCharm.class,
        FireWhip.class, BullElephant.class})
class FreewindFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Red flyer cannot block Freewind Falcon")
    void redCreatureCannotBlock() {
        addCreatureReady(player1, new FreewindFalcon());
        addCreatureReady(player2, new ViashivanDragon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

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
        Permanent falcon = addCreatureReady(player2, new FreewindFalcon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(falcon.getMarkedDamage()).isZero();
        assertThat(dragon.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Viashivan Dragon");
        harness.assertOnBattlefield(player2, "Freewind Falcon");
    }

    @Test
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

    @Test
    @DisplayName("A nonred creature without flying or reach cannot block Freewind Falcon")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new FreewindFalcon());
        addCreatureReady(player2, new BullElephant());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Nonred flyers can block and deal lethal damage to Freewind Falcon")
    void nonredFlyingCombatDamageIsNotPrevented() {
        addCreatureReady(player1, new FreewindFalcon());
        addCreatureReady(player2, new FreewindFalcon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertNotOnBattlefield(player1, "Freewind Falcon");
        harness.assertNotOnBattlefield(player2, "Freewind Falcon");
        harness.assertInGraveyard(player1, "Freewind Falcon");
        harness.assertInGraveyard(player2, "Freewind Falcon");
    }
}
