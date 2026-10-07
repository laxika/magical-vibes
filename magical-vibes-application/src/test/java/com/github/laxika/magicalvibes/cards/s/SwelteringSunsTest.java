package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwelteringSuns.class, GrizzlyBears.class, GiantSpider.class})
class SwelteringSunsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each creature: kills 2/2s on both sides, spares a 2/4")
    void dealsThreeToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new SwelteringSuns()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // A 2/4 survives 3 damage.
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SwelteringSuns()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SwelteringSuns()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sweltering Suns");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Surviving creatures retain three marked damage")
    void marksDamageOnSurvivors() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new SwelteringSuns()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Giant Spider").getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Sweltering Suns");
    }

    @Test
    @DisplayName("Cycling pays the discard immediately and draws only on resolution without dealing damage")
    void cyclingDiscardsAsCostWithoutSweepingCreatures() {
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new SwelteringSuns()));
        harness.setLibrary(player1, List.of(new GiantSpider(), new SwelteringSuns()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Sweltering Suns");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Giant Spider");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Giant Spider").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Giant Spider").getMarkedDamage()).isZero();
    }
}
