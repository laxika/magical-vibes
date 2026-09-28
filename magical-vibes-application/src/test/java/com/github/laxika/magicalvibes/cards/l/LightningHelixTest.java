package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LoxodonHierarch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningHelix.class, LoxodonHierarch.class, Forest.class})
class LightningHelixTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerAndGainsLife() {
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 18);
    }

    @Test
    void dealsDamageToCreatureAndGainsLife() {
        Permanent hierarch = harness.addToBattlefieldAndReturn(player2, new LoxodonHierarch());
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, hierarch.getId());

        harness.assertOnBattlefield(player2, "Loxodon Hierarch");
        assertThat(hierarch.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 18);
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
        Permanent hierarch = harness.addToBattlefieldAndReturn(player2, new LoxodonHierarch());
        harness.setHand(player1, List.of(new LightningHelix()));
        addLightningHelixMana();
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, hierarch.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    private void addLightningHelixMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
