package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicSleuth.class, GrizzlyBears.class, Unsummon.class, WrathOfGod.class})
class AngelicSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when another permanent you control leaves with a counter")
    void investigatesWhenCounteredPermanentLeaves() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when the departing permanent has no counters")
    void doesNotInvestigateWithoutCounters() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate when an opponent's permanent leaves")
    void doesNotInvestigateForOpponentPermanent() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate for its own departure even with counters")
    void doesNotInvestigateForItself() {
        Permanent sleuth = harness.addToBattlefieldAndReturn(player1, new AngelicSleuth());
        sleuth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        bouncePermanent(sleuth, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Investigates once per departing permanent regardless of counter number or type")
    void investigatesOnceForMultipleNonPowerCounters() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 3);
        bears.setCounterCount(CounterType.STUN, 2);

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates for each countered creature dying simultaneously with the Sleuth")
    void investigatesWhenSleuthDiesWithCounteredCreatures() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angelic Sleuth")).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bouncePermanent(bears, player1);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1).contains(drawnCard);
    }

    @Test
    @DisplayName("Investigates when a countered noncreature token is sacrificed")
    void investigatesForCounteredClueToken() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bouncePermanent(bears, player1);
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        clue.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue").getFirst().getId()).isNotEqualTo(clue.getId());
    }

    private void bouncePermanent(Permanent permanent, com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new Unsummon()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(caster, 0, permanent.getId());
        harness.passBothPriorities();
    }
}
