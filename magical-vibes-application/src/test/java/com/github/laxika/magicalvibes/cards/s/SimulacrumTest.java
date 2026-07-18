package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulacrumTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to damage dealt to you this turn and deals that much to your creature")
    void gainsLifeAndDamagesOwnCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        shockSelf();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        castSimulacrum(spider.getId());

        // 2 damage dealt to player1 this turn -> gain 2 life (18 -> 20) and deal 2 to the 2/4 spider.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("With no damage taken this turn, gains no life and deals no damage")
    void noDamageMeansNoEffect() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castSimulacrum(spider.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Simulacrum()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void shockSelf() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
    }

    private void castSimulacrum(UUID targetCreatureId) {
        harness.setHand(player1, List.of(new Simulacrum()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();
    }
}
