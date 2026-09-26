package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Blight;
import com.github.laxika.magicalvibes.cards.d.DemonicTorment;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.u.Urborg;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamsesOverdark.class, Blight.class, DemonicTorment.class, DurkwoodBoars.class, Urborg.class})
class RamsesOverdarkTest extends BaseCardTest {

    @Test
    void tapsAndDestroysTargetEnchantedCreature() {
        Permanent ramses = addReadyRamses();
        Permanent target = addEnchantedBoars();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(ramses.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    void cannotTargetUnenchantedCreature() {
        Permanent ramses = addReadyRamses();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted creature");
        assertThat(ramses.isTapped()).isFalse();
    }

    @Test
    void cannotTargetEnchantedNoncreature() {
        Permanent ramses = addReadyRamses();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Urborg());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Blight());
        aura.setAttachedTo(land.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted creature");
        assertThat(ramses.isTapped()).isFalse();
    }

    private Permanent addReadyRamses() {
        return addCreatureReady(player1, new RamsesOverdark());
    }

    private Permanent addEnchantedBoars() {
        Permanent boars = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DemonicTorment());
        aura.setAttachedTo(boars.getId());
        return boars;
    }
}
