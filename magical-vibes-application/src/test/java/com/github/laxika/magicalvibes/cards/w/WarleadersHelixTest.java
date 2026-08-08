package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

class WarleadersHelixTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player and controller gains 4 life")
    void deals4DamageToPlayerAndGains4Life() {
        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 4-toughness creature")
    void deals4DamageToCreatureAndKillsIt() {
        Permanent elemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");

        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Fizzles when the target creature leaves before resolution — no life gain")
    void fizzlesWhenTargetCreatureRemoved() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }
}
