package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BywayCourier;
import com.github.laxika.magicalvibes.cards.r.RottenheartGhoul;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafColossus.class, RottenheartGhoul.class, BywayCourier.class, DeadWeight.class})
class DiregrafColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each Zombie card in controller's graveyard")
    void entersWithCountersPerZombieCard() {
        harness.setGraveyard(player1, List.of(new RottenheartGhoul(), new RottenheartGhoul(), new BywayCourier(), new DeadWeight()));

        castColossus();

        Permanent colossus = findPermanent(player1, "Diregraf Colossus");
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a Zombie spell creates a tapped Zombie token")
    void zombieSpellCreatesTappedToken() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.castFromHand(player1, new RottenheartGhoul(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rottenheart Ghoul")).isZero();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rottenheart Ghoul");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Zombie creature does not create a token")
    void nonZombieSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.castFromHand(player1, new BywayCourier(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Enters without counters when its controller's graveyard is empty")
    void entersWithoutCountersWithEmptyGraveyard() {
        castColossus();

        assertThat(findPermanent(player1, "Diregraf Colossus")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Does not count Zombie cards in the opponent's graveyard")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new RottenheartGhoul()));
        harness.setGraveyard(player2, List.of(new RottenheartGhoul(), new DiregrafColossus()));

        castColossus();

        assertThat(findPermanent(player1, "Diregraf Colossus")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts Zombie cards when entering rather than when cast")
    void countsGraveyardAtEntry() {
        harness.castFromHand(player1, new DiregrafColossus(), "{2}{B}");
        harness.setGraveyard(player1, List.of(new RottenheartGhoul(), new DiregrafColossus()));

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Diregraf Colossus")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent casting a Zombie spell does not create a token")
    void opponentZombieSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new RottenheartGhoul(), "{3}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rottenheart Ghoul");
        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Each existing Colossus triggers when another Colossus is cast")
    void existingColossiEachTriggerForAnotherColossus() {
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.addToBattlefield(player1, new DiregrafColossus());
        harness.castFromHand(player1, new DiregrafColossus(), "{2}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Diregraf Colossus")).isEqualTo(2);
        assertThat(findPermanents(player1, "Zombie")).hasSize(2)
                .allSatisfy(zombie -> assertThat(zombie.isTapped()).isTrue());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Diregraf Colossus")).isEqualTo(3);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    private void castColossus() {
        harness.castFromHand(player1, new DiregrafColossus(), "{2}{B}");
        harness.passBothPriorities();
    }
}
