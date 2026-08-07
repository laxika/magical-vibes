package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbermawHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Red spell you control deals 1 extra damage to a player")
    void redSpellDealsExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Shock deals 2 + 1 = 3
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Red spell you control deals 1 extra damage to a creature")
    void redSpellDealsExtraDamageToCreature() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();

        // Two boosted Shocks deal 3 + 3 = 6 to a 4/4
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Nonred spell you control gets no bonus")
    void nonRedSpellGetsNoBonus() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another red creature you control deals 1 extra combat damage")
    void anotherRedCreatureDealsExtraCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion());
        addCreatureReady(player1, new RagingGoblin()); // 1/1

        declareAttackers(player1, List.of(1));

        // Raging Goblin deals 1 + 1 = 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Embermaw Hellion does not boost its own combat damage")
    void doesNotBoostItsOwnCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion()); // 4/5

        declareAttackers(player1, List.of(0));

        // "another red source" — the Hellion's own 4 damage is unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Two Embermaw Hellions each boost the other's combat damage")
    void twoHellionsBoostEachOther() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion());
        addCreatureReady(player1, new EmbermawHellion());

        declareAttackers(player1, List.of(0));

        // The attacking Hellion deals 4 + 1 = 5 (boosted only by the other one)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Opponent's red source is not boosted")
    void opponentsRedSourceNotBoosted() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
