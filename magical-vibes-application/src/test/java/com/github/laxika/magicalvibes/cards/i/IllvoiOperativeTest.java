package com.github.laxika.magicalvibes.cards.i;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllvoiOperative.class, Shock.class})
class IllvoiOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller casts their second spell")
    void secondSpellPutsCounterOnItself() {
        Permanent operative = addCreatureReady(player1, new IllvoiOperative());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(IllvoiOperative.class)
    void itsOwnCastCountsTowardTheSecondSpell() {
        harness.setHand(player1, List.of(new IllvoiOperative(), new IllvoiOperative()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent first = findPermanent(player1, "Illvoi Operative");
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Illvoi Operative"))
                .hasSize(2)
                .filteredOn(permanent -> permanent != first)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void enteringAsTheSecondSpellDoesNotTriggerOnItselfOrTheThirdSpell() {
        harness.setHand(player1, List.of(new Shock(), new IllvoiOperative(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent operative = findPermanent(player1, "Illvoi Operative");
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed(IllvoiOperative.class)
    void opponentsSpellsDoNotTriggerIt() {
        Permanent operative = addCreatureReady(player1, new IllvoiOperative());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new IllvoiOperative(), new IllvoiOperative()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Illvoi Operative"))
                .hasSize(2)
                .anySatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void spellCountResetsAndItTriggersDuringOpponentsTurn() {
        Permanent operative = addCreatureReady(player1, new IllvoiOperative());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removingTheSourceBeforeTheTriggerResolvesDoesNotPutACounterOnIt() {
        Permanent operative = addCreatureReady(player1, new IllvoiOperative());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castInstant(player2, 0, operative.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(operative);
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
