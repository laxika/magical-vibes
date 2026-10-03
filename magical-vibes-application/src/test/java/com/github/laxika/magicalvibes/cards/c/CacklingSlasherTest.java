package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacklingSlasher.class, GrizzlyBears.class, Shock.class})
class CacklingSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without a +1/+1 counter when no creature died this turn")
    void entersWithoutCounterWhenNoCreatureDied() {
        castSlasher();

        Permanent slasher = findSlasher();
        assertThat(slasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(slasher.getEffectivePower()).isEqualTo(3);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter when a creature died this turn")
    void entersWithCounterAfterCreatureDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new CacklingSlasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent slasher = findSlasher();
        assertThat(slasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(slasher.getEffectivePower()).isEqualTo(4);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void entersWithCounterAfterOwnCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");

        castSlasher();

        assertThat(findSlasher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleDeathsStillGiveOnlyOneCounter() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, ownBears.getId());
        harness.castAndResolveInstant(player1, 0, opposingBears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        castSlasher();

        assertThat(findSlasher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureDyingInResponseToSlasherCountsAtEntry() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingSlasher(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Cackling Slasher");

        harness.passBothPriorities();

        assertThat(findSlasher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureDeathOnPreviousTurnDoesNotCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        castSlasher();

        assertThat(findSlasher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private void castSlasher() {
        harness.setHand(player1, List.of(new CacklingSlasher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findSlasher() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cackling Slasher"))
                .findFirst()
                .orElseThrow();
    }
}
