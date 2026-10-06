package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreSiegebreaker.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class OgreSiegebreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature that was dealt damage this turn")
    void destroysDamagedCreature() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(ogre), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnDamagedCreature() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a creature after actual noncombat damage")
    void destroysCreatureAfterShock() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OgreSiegebreaker());
        shock(target);
        harness.assertOnBattlefield(player2, "Ogre Siegebreaker");
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ogre Siegebreaker");
        harness.assertInGraveyard(player2, "Ogre Siegebreaker");
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        ogre.tap();
        ogre.setSummoningSick(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new OgreSiegebreaker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new OgreSiegebreaker());
        shock(first);
        shock(second);
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, first.getId());
        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ogre Siegebreaker");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Ogre Siegebreaker");
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OgreSiegebreaker());
        shock(target);
        addActivationMana();
        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, target.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, ogre.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ogre Siegebreaker");
        harness.assertNotOnBattlefield(player2, "Ogre Siegebreaker");
        harness.assertInGraveyard(player2, "Ogre Siegebreaker");
    }

    @Test
    @DisplayName("Does not destroy a target returned to hand in response")
    void doesNotDestroyTargetThatLeavesBattlefield() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreSiegebreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OgreSiegebreaker());
        shock(target);
        addActivationMana();
        harness.activateAbility(player1, battlefieldIndex(ogre), 0, null, target.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ogre Siegebreaker");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ogre Siegebreaker");
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
