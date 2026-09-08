package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PestilentHazeTest extends BaseCardTest {

    @Test
    @DisplayName("First mode gives -2/-2 to all creatures")
    void debuffsAllCreatures() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPestilentHaze(0);

        assertThat(ownGiant.getEffectivePower()).isEqualTo(1);
        assertThat(ownGiant.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBears);
    }

    @Test
    @DisplayName("Second mode removes two loyalty counters from each planeswalker")
    void removesLoyaltyCountersFromAllPlaneswalkers() {
        Permanent ownPlaneswalker = addReadyPlaneswalker(player1, 5);
        Permanent opponentPlaneswalker = addReadyPlaneswalker(player2, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPestilentHaze(1);

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Second mode removes only the loyalty counters a planeswalker has")
    void removesAtMostAvailableLoyaltyCounters() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 1);

        castPestilentHaze(1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    private void castPestilentHaze(int modeIndex) {
        harness.setHand(player1, List.of(new PestilentHaze()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{modeIndex}, List.of());
        harness.passBothPriorities();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = new Permanent(new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
