package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BazaarTraderTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains control of a creature you control")
    void gainsControlOfCreature() {
        Permanent trader = addTrader();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        activateFor(creature);

        assertThat(trader.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Target player gains control of an artifact or land you control")
    void gainsControlOfArtifactAndLand() {
        addTrader();
        Permanent artifact = new Permanent(new Millstone());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        gd.playerBattlefields.get(player1.getId()).add(land);

        activateFor(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);

        Permanent secondTrader = addTrader();
        int traderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondTrader);
        harness.activateAbilityWithMultiTargets(player1, traderIndex, 0, List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(secondTrader.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("A permanent you control must be an artifact, creature, or land")
    void rejectsOtherPermanentTypes() {
        addTrader();
        Permanent aura = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThatThrownBy(() -> activateFor(aura))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land you control");
    }

    private Permanent addTrader() {
        return addCreatureReady(player1, new BazaarTrader());
    }

    private void activateFor(Permanent target) {
        int traderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Bazaar Trader"));
        harness.activateAbilityWithMultiTargets(player1, traderIndex, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
