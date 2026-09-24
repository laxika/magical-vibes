package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaCaptain.class, SoulWarden.class, FaerieMiscreant.class, FugitiveWizard.class})
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
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castEmeriaCaptain();

        Permanent captain = findPermanent(player1, "Emeria Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void castEmeriaCaptain() {
        harness.setHand(player1, List.of(new EmeriaCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
