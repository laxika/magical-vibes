package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntrepidHeroTest extends BaseCardTest {

    private Permanent setup() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new IntrepidHero());
        hero.setSummoningSick(false);
        return hero;
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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
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
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent hero = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
