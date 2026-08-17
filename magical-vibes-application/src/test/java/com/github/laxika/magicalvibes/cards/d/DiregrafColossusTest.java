package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZombieMob;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiregrafColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each Zombie card in controller's graveyard")
    void entersWithCountersPerZombieCard() {
        harness.setGraveyard(player1, List.of(new DiregrafCaptain(), new DiregrafCaptain(), new GrizzlyBears(), new Shock()));

        castColossus();

        Permanent colossus = findPermanent(player1, "Diregraf Colossus");
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a Zombie spell creates a tapped Zombie token")
    void zombieSpellCreatesTappedToken() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.setHand(player1, List.of(new ZombieMob()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie).isNotNull();
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-Zombie creature does not create a token")
    void nonZombieSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    private void castColossus() {
        harness.setHand(player1, List.of(new DiregrafColossus()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
