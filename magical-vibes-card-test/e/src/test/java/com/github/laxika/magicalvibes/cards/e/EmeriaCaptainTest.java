package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.cards.m.MerfolkWindrobber;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaCaptain.class, KorCelebrant.class, MerfolkWindrobber.class,
        ExpeditionDiviner.class, IntoTheRoil.class})
class EmeriaCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one +1/+1 counter on itself when it is the only party creature")
    void etbCountsItselfAsWarrior() {
        castEmeriaCaptain();

        Permanent captain = findPermanent(player1, "Emeria Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts counters on itself equal to the size of its party")
    void etbPutsCountersEqualToPartySize() {
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new MerfolkWindrobber());
        harness.addToBattlefield(player1, new ExpeditionDiviner());

        castEmeriaCaptain();

        Permanent captain = findPermanent(player1, "Emeria Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void duplicateRolesCountOnlyOnce() {
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new EmeriaCaptain());

        castEmeriaCaptain();

        assertThat(findPermanents(player1, "Emeria Captain"))
                .extracting(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactly(0, 2);
    }

    @Test
    void opposingCreaturesDoNotCountTowardParty() {
        harness.addToBattlefield(player2, new KorCelebrant());
        harness.addToBattlefield(player2, new MerfolkWindrobber());
        harness.addToBattlefield(player2, new ExpeditionDiviner());

        castEmeriaCaptain();

        assertThat(findPermanent(player1, "Emeria Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void partyIsCountedWhenTriggerResolves() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new MerfolkWindrobber());
        castCaptainWithoutResolvingTrigger();
        assertThat(findPermanent(player1, "Emeria Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, rogue.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Emeria Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void departedCaptainCannotPutCountersOnAnotherCaptain() {
        harness.addToBattlefield(player1, new EmeriaCaptain());
        castCaptainWithoutResolvingTrigger();
        Permanent enteringCaptain = findPermanents(player1, "Emeria Captain").get(1);

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, enteringCaptain.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Emeria Captain")).isEqualTo(1);
        assertThat(findPermanent(player1, "Emeria Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castEmeriaCaptain() {
        castCaptainWithoutResolvingTrigger();
        resolveAllTriggers();
    }

    private void castCaptainWithoutResolvingTrigger() {
        harness.setHand(player1, List.of(new EmeriaCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
