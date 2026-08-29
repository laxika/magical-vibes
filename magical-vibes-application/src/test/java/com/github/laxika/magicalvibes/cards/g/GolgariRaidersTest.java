package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GolgariRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in its controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        castRaiders();

        Permanent raiders = findRaiders();
        assertThat(raiders).isNotNull();
        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(raiders.getEffectivePower()).isEqualTo(2);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creature cards in its controller's graveyard it enters as a 0/0 and dies")
    void diesWithNoCreatureCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        castRaiders();

        harness.assertNotOnBattlefield(player1, "Golgari Raiders");
    }

    @Test
    @DisplayName("Haste allows it to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        harness.setLife(player2, 20);

        castRaiders();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void castRaiders() {
        harness.setHand(player1, List.of(new GolgariRaiders()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findRaiders() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golgari Raiders"))
                .findFirst().orElse(null);
    }
}
