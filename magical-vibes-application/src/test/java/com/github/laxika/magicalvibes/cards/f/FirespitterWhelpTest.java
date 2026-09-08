package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DragonHatchling;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FirespitterWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell deals 1 damage to each opponent")
    void noncreatureSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a Dragon creature spell deals 1 damage to each opponent")
    void dragonSpellDealsDamage() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new DragonHatchling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a non-Dragon creature spell does not trigger Firespitter Whelp")
    void nonDragonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FirespitterWhelp());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
    }
}
