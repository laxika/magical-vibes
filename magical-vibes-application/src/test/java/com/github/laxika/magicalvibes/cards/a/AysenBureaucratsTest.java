package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AysenBureaucratsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with power 2 or less")
    void resolvingTapsLowPowerCreature() {
        addReadyBureaucrats(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps the Bureaucrats")
    void activatingTapsSelf() {
        Permanent bureaucrats = addReadyBureaucrats(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(bureaucrats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addReadyBureaucrats(player1);
        Permanent giant = addReady(new HillGiant(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBureaucrats(Player player) {
        return addReady(new AysenBureaucrats(), player);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card, Player player) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
