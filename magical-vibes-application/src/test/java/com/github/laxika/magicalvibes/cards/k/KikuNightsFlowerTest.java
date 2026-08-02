package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KikuNightsFlowerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability kills a 2/2 which deals 2 damage to itself")
    void killsCreatureWhenPowerIsLethal() {
        harness.addToBattlefieldAndReturn(player1, new KikuNightsFlower()).setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A 3/5 survives with 3 marked damage")
    void survivesWhenPowerIsBelowToughness() {
        harness.addToBattlefieldAndReturn(player1, new KikuNightsFlower()).setSummoningSick(false);
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating taps Kiku")
    void activationTapsKiku() {
        harness.addToBattlefieldAndReturn(player1, new KikuNightsFlower()).setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Kiku, Night's Flower").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefieldAndReturn(player1, new KikuNightsFlower()).setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plainsId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Plains");
    }
}
