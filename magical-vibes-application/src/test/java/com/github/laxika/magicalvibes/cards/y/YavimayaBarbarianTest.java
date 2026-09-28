package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.cards.s.ShorelineRaider;
import com.github.laxika.magicalvibes.cards.s.ShimmeringWings;
import com.github.laxika.magicalvibes.cards.s.StormscapeApprentice;
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

@CardUsed({YavimayaBarbarian.class, Repulse.class, ShorelineRaider.class, ShimmeringWings.class,
        StormscapeApprentice.class})
class YavimayaBarbarianTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Yavimaya Barbarian")
    void blueCreatureCannotBlock() {
        Permanent barbarian = addCreatureReady(player1, new YavimayaBarbarian());
        barbarian.setAttacking(true);
        addCreatureReady(player2, new ShorelineRaider());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Yavimaya Barbarian cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent barbarian = addCreatureReady(player2, new YavimayaBarbarian());

        harness.setHand(player1, List.of(new Repulse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, barbarian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Yavimaya Barbarian cannot be enchanted by a blue Aura")
    void cannotBeEnchantedByBlueAura() {
        Permanent barbarian = addCreatureReady(player2, new YavimayaBarbarian());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, barbarian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Yavimaya Barbarian cannot be targeted by a blue creature's ability")
    void cannotBeTargetedByBlueAbility() {
        addCreatureReady(player1, new StormscapeApprentice());
        Permanent barbarian = addCreatureReady(player2, new YavimayaBarbarian());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, barbarian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Yavimaya Barbarian takes no combat damage from a blue creature")
    void takesNoCombatDamageFromBlueCreature() {
        addCreatureReady(player2, new ShorelineRaider());
        addCreatureReady(player1, new YavimayaBarbarian());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
    }
}
