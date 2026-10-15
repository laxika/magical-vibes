package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShipwreckSinger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenthicGiant.class, GiantGrowth.class, Shock.class, ShipwreckSinger.class})
class BenthicGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Benthic Giant with spells")
    void opponentCannotTargetWithSpells() {
        Permanent giant = addCreatureReady(player1, new BenthicGiant());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, giant.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Benthic Giant with spells")
    void controllerCanTargetOwnGiant() {
        Permanent giant = addCreatureReady(player1, new BenthicGiant());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, giant.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent cannot target Benthic Giant with activated abilities")
    void opponentCannotTargetWithActivatedAbilities() {
        Permanent giant = addCreatureReady(player1, new BenthicGiant());
        addCreatureReady(player2, new ShipwreckSinger());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
