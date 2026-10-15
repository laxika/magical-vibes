package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VindictiveVampire.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class VindictiveVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control dying damages each opponent and gains you life")
    void anotherCreatureYouControlDies() {
        harness.addToBattlefield(player1, new VindictiveVampire());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Vindictive Vampire")
    void opponentCreatureDies() {
        harness.addToBattlefield(player1, new VindictiveVampire());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Vindictive Vampire does not trigger for its own death")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new VindictiveVampire());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castWrath();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vindictive Vampire");
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once per other allied creature even when the Vampire dies")
    void simultaneousDeathsTriggerForEachOtherAlly() {
        harness.addToBattlefield(player1, new VindictiveVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castWrath();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vindictive Vampire");
        harness.assertLife(player1, controllerLifeBefore + 2);
        harness.assertLife(player2, opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Two Vampires dying together each trigger for the other Vampire")
    void twoVampiresDyingTogetherTriggerForEachOther() {
        harness.addToBattlefield(player1, new VindictiveVampire());
        harness.addToBattlefield(player1, new VindictiveVampire());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castWrath();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, controllerLifeBefore + 2);
        harness.assertLife(player2, opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("A pending death trigger resolves after its Vampire is removed")
    void pendingTriggerResolvesAfterSourceDies() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VindictiveVampire());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        assertThat(gd.stack).hasSize(1);
        killWithShock(player1, vampire);
        killWithShock(player1, vampire);
        harness.assertInGraveyard(player1, "Vindictive Vampire");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore + 1);
        harness.assertLife(player2, opponentLifeBefore - 1);
    }

    private void castWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
    }
}
