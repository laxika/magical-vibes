package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasEmbrace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LushGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavaenElves.class, Forest.class, LushGrowth.class, GaeasEmbrace.class, GrizzlyBears.class})
class SavaenElvesTest extends BaseCardTest {

    @Test
    void destroysAuraAttachedToLand() {
        addReadySavaenElves(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LushGrowth());
        aura.setAttachedTo(land.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lush Growth");
        harness.assertInGraveyard(player2, "Lush Growth");
    }

    @Test
    void cannotTargetAuraAttachedToCreature() {
        addReadySavaenElves(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GaeasEmbrace());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a land");
    }

    @Test
    void cannotTargetUnattachedAura() {
        addReadySavaenElves(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LushGrowth());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a land");
    }

    private Permanent addReadySavaenElves(Player player) {
        Permanent elves = harness.addToBattlefieldAndReturn(player, new SavaenElves());
        elves.setSummoningSick(false);
        return elves;
    }
}
