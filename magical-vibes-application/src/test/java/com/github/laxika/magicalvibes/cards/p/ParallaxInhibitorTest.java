package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoltingMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallaxInhibitorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a fade counter on each fading permanent you control")
    void putsFadeCountersOnControlledFadingPermanents() {
        addReadyInhibitor(player1);
        Permanent fadingPermanent = addReadyPermanent(player1, new JoltingMerfolk());
        Permanent nonFadingPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentFadingPermanent = addReadyPermanent(player2, new JoltingMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fadingPermanent.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(nonFadingPermanent.getCounterCount(CounterType.FADE)).isZero();
        assertThat(opponentFadingPermanent.getCounterCount(CounterType.FADE)).isZero();
        harness.assertNotOnBattlefield(player1, "Parallax Inhibitor");
        harness.assertInGraveyard(player1, "Parallax Inhibitor");
    }

    @Test
    @DisplayName("Requires one generic mana to activate")
    void cannotActivateWithoutMana() {
        addReadyInhibitor(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyInhibitor(Player player) {
        addReadyPermanent(player, new ParallaxInhibitor());
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
