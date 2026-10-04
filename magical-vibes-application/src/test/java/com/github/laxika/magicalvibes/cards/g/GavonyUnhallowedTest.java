package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GavonyUnhallowed.class, GrizzlyBears.class, Shock.class})
class GavonyUnhallowedTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature you control dies")
    void putsCounterWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new GavonyUnhallowed());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent gavonyUnhallowed = findPermanent(player1, "Gavony Unhallowed");
        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        killCreature(player1, "Grizzly Bears", player2);
        harness.passBothPriorities();

        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GavonyUnhallowed());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent gavonyUnhallowed = findPermanent(player1, "Gavony Unhallowed");
        killCreature(player2, "Grizzly Bears", player1);

        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each allied creature death creates a separate counter trigger")
    void putsOneCounterForEachDeath() {
        harness.addToBattlefield(player1, new GavonyUnhallowed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent gavonyUnhallowed = findPermanent(player1, "Gavony Unhallowed");

        killCreature(player1, "Grizzly Bears", player2);
        killCreature(player1, "Grizzly Bears", player2);

        assertThat(gd.stack).hasSize(2);
        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gavonyUnhallowed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its own death does not trigger its ability")
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new GavonyUnhallowed());

        killCreature(player1, "Gavony Unhallowed", player2);
        killCreature(player1, "Gavony Unhallowed", player2);

        harness.assertInGraveyard(player1, "Gavony Unhallowed");
        harness.assertNotOnBattlefield(player1, "Gavony Unhallowed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger cannot put a counter on a different Gavony Unhallowed")
    void pendingTriggerDoesNotAffectAnotherPermanent() {
        harness.addToBattlefield(player1, new GavonyUnhallowed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        killCreature(player1, "Grizzly Bears", player2);
        assertThat(gd.stack).hasSize(1);

        killCreature(player1, "Gavony Unhallowed", player2);
        killCreature(player1, "Gavony Unhallowed", player2);
        harness.assertNotOnBattlefield(player1, "Gavony Unhallowed");
        harness.addToBattlefield(player1, new GavonyUnhallowed());
        Permanent replacement = findPermanent(player1, "Gavony Unhallowed");

        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void killCreature(Player player, String name, Player caster) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID permanentId = harness.getPermanentId(player, name);
        harness.castAndResolveInstant(caster, 0, permanentId);
    }
}
