package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.CastleRaptors;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantDoomsayers.class, BenalishCavalry.class, CastleRaptors.class, Plains.class})
class ErrantDoomsayersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with toughness 2 or less")
    void resolvingTapsLowToughnessCreature() {
        addCreatureReady(player1, new ErrantDoomsayers());
        Permanent target = addCreatureReady(player2, new BenalishCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Errant Doomsayers")
    void activatingTapsSelf() {
        Permanent doomsayers = addCreatureReady(player1, new ErrantDoomsayers());
        Permanent target = addCreatureReady(player2, new BenalishCavalry());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(doomsayers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 2")
    void cannotTargetHighToughnessCreature() {
        addCreatureReady(player1, new ErrantDoomsayers());
        Permanent giant = addCreatureReady(player2, new CastleRaptors());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new ErrantDoomsayers());
        Permanent target = addCreatureReady(player1, new BenalishCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ErrantDoomsayers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
