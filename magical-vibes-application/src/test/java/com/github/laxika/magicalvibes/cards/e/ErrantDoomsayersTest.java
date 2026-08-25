package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantDoomsayers.class, GrizzlyBears.class, HillGiant.class})
class ErrantDoomsayersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with toughness 2 or less")
    void resolvingTapsLowToughnessCreature() {
        addReadyDoomsayers(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Errant Doomsayers")
    void activatingTapsSelf() {
        Permanent doomsayers = addReadyDoomsayers(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(doomsayers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 2")
    void cannotTargetHighToughnessCreature() {
        addReadyDoomsayers(player1);
        Permanent giant = addReady(new HillGiant(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDoomsayers(Player player) {
        return addReady(new ErrantDoomsayers(), player);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card, Player player) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
