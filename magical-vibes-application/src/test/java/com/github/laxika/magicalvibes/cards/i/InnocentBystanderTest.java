package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnocentBystander.class, Shock.class, LightningBolt.class})
class InnocentBystanderTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when dealt 3 or more damage")
    void investigatesWhenDealtAtLeastThreeDamage() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when dealt less than 3 damage")
    void doesNotInvestigateWhenDealtLessThanThreeDamage() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }
}
