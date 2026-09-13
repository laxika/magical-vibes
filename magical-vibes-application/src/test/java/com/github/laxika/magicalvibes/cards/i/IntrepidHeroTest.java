package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Castle;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidHero.class, AirElemental.class, HillGiant.class, Castle.class})
class IntrepidHeroTest extends BaseCardTest {

    private Permanent setup() {
        return addCreatureReady(player1, new IntrepidHero());
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Destroys a creature with power 4 or greater")
    void destroysHighPowerCreature() {
        Permanent hero = setup();
        harness.addToBattlefield(player2, new AirElemental());
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, idxOf(hero), 0, null, elementalId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can destroy a high-power creature it controls")
    void destroysOwnHighPowerCreature() {
        Permanent hero = setup();
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, idxOf(hero), 0, null, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent hero = setup();
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, giantId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent hero = setup();
        harness.addToBattlefield(player2, new Castle());
        UUID castleId = harness.getPermanentId(player2, "Castle");

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, castleId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent hero = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
