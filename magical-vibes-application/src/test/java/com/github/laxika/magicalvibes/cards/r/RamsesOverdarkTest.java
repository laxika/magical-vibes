package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerrasEmbrace;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamsesOverdark.class, GrizzlyBears.class, SerrasEmbrace.class})
class RamsesOverdarkTest extends BaseCardTest {

    @Test
    void tapsAndDestroysTargetEnchantedCreature() {
        Permanent ramses = addReadyRamses();
        Permanent target = addEnchantedBears();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(ramses.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetUnenchantedCreature() {
        addReadyRamses();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted creature");
    }

    private Permanent addReadyRamses() {
        Permanent ramses = harness.addToBattlefieldAndReturn(player1, new RamsesOverdark());
        ramses.setSummoningSick(false);
        return ramses;
    }

    private Permanent addEnchantedBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SerrasEmbrace());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
