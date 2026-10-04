package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DauntlessEscort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FightToTheDeath.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        TrollAscetic.class, Unsummon.class, DauntlessEscort.class})
class FightToTheDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys blocked attackers and blocking creatures, spares uninvolved creatures")
    void destroysBlockedAndBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        // Not in combat — should survive.
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spares an unblocked attacker and creatures not in combat")
    void sparesUnblockedAndNonCombatants() {
        Permanent unblockedAttacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        unblockedAttacker.setSummoningSick(false);
        unblockedAttacker.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a blocked attacker after its only blocker returns to hand")
    void destroysAttackerAfterBlockerLeaves() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new Unsummon(), new FightToTheDeath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A blocker regenerates but its blocked attacker is still destroyed")
    void regenerationDoesNotSpareBlockedAttacker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotInGraveyard(player1, "Troll Ascetic");
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("An indestructible blocker survives while its blocked attacker is destroyed")
    void respectsIndestructible() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dauntless Escort");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Resolves without targets when there are no creatures")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Fight to the Death");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
