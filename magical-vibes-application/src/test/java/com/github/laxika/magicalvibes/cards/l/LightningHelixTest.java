package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningHelix.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class LightningHelixTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerAndGainsLife() {
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void dealsDamageToCreatureAndGainsLife() {
        Permanent elemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(elemental);
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void rejectsLandAsAnyTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    void fizzlesWithoutLifeGainWhenTargetIsRemoved() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    private void addLightningHelixMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
