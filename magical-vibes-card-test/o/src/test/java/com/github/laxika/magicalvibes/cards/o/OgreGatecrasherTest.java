package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SilkwingScout;
import com.github.laxika.magicalvibes.cards.w.WakestoneGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreGatecrasher.class, WakestoneGargoyle.class, SilkwingScout.class})
class OgreGatecrasherTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it destroys target creature with defender")
    void entersAndDestroysCreatureWithDefender() {
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new WakestoneGargoyle());

        castOgreGatecrasher(defender.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wakestone Gargoyle");
        harness.assertInGraveyard(player2, "Wakestone Gargoyle");
        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
    }

    @Test
    @DisplayName("It can destroy a defender controlled by its controller")
    void entersAndDestroysItsControllersDefender() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new WakestoneGargoyle());

        castOgreGatecrasher(defender.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wakestone Gargoyle");
        harness.assertInGraveyard(player1, "Wakestone Gargoyle");
        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
    }

    @Test
    @DisplayName("It cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SilkwingScout());
        harness.setHand(player1, List.of(new OgreGatecrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("defender");
    }

    @Test
    @DisplayName("It enters without an ETB trigger when no creature has defender")
    void entersWithoutTargetWhenNoCreatureHasDefender() {
        harness.addToBattlefield(player2, new SilkwingScout());

        harness.castFromHand(player1, new OgreGatecrasher(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
        harness.assertOnBattlefield(player2, "Silkwing Scout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A defender allowed to attack remains a legal destruction target")
    void destroysDefenderAllowedToAttack() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new WakestoneGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        castOgreGatecrasher(defender.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wakestone Gargoyle");
        harness.assertNotOnBattlefield(player1, "Wakestone Gargoyle");
        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
    }

    @Test
    @DisplayName("The trigger does not destroy another defender when its target leaves")
    void targetLeavingDoesNotRedirectDestruction() {
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new WakestoneGargoyle());
        Permanent otherDefender = harness.addToBattlefieldAndReturn(player2, new WakestoneGargoyle());
        castOgreGatecrasher(defender.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(defender);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherDefender);
        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
        assertThat(gd.stack).isEmpty();
    }

    private void castOgreGatecrasher(UUID targetId) {
        harness.setHand(player1, List.of(new OgreGatecrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetId);
    }
}
