package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brainwash;
import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.g.GoblinCaves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavaenElves.class, CityOfShadows.class, GoblinCaves.class, Brainwash.class, Squire.class})
class SavaenElvesTest extends BaseCardTest {

    @Test
    void destroysAuraAttachedToLand() {
        Permanent elves = addCreatureReady(player1, new SavaenElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GoblinCaves());
        aura.setAttachedTo(land.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId());
        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Caves");
        harness.assertInGraveyard(player2, "Goblin Caves");
    }

    @Test
    void cannotTargetAuraAttachedToCreature() {
        addCreatureReady(player1, new SavaenElves());
        Permanent creature = addCreatureReady(player2, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Brainwash());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a land");
    }

    @Test
    void cannotTargetUnattachedAura() {
        addCreatureReady(player1, new SavaenElves());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GoblinCaves());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a land");
    }

    @Test
    void cannotTargetLandItself() {
        addCreatureReady(player1, new SavaenElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a land");
    }
}
