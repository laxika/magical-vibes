package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndergrowthScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in any graveyard")
    void entersWithCountersPerCreatureCardInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock()); // not a creature card

        castScavenger();

        Permanent scavenger = findScavenger();
        assertThat(scavenger).isNotNull();
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(scavenger.getEffectivePower()).isEqualTo(3);
        assertThat(scavenger.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With no creature cards in any graveyard it enters as a 0/0 and dies")
    void diesWithNoCreatureCardsInGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        castScavenger();

        harness.assertNotOnBattlefield(player1, "Undergrowth Scavenger");
    }

    private void castScavenger() {
        harness.setHand(player1, List.of(new UndergrowthScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve the creature spell
    }

    private Permanent findScavenger() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Undergrowth Scavenger"))
                .findFirst().orElse(null);
    }
}
