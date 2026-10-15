package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaWorldsoulSpeaker.class, Forest.class, GrizzlyBears.class, Shock.class})
class NissaWorldsoulSpeakerTest extends BaseCardTest {

    @Test
    void landfallGivesTwoEnergyCounters() {
        harness.addToBattlefield(player1, new NissaWorldsoulSpeaker());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEightEnergyForPermanentSpell() {
        harness.addToBattlefield(player1, new NissaWorldsoulSpeaker());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotUseEnergyForNonPermanentSpell() {
        harness.addToBattlefield(player1, new NissaWorldsoulSpeaker());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayPermanentSpellWithInsufficientEnergy() {
        harness.addToBattlefield(player1, new NissaWorldsoulSpeaker());
        gd.playerEnergyCounters.put(player1.getId(), 7);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
