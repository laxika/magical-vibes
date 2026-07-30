package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OmniscienceTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spell can be cast from hand with no mana available")
    void creatureCastForFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature spell can also be cast from hand for free")
    void sorceryCastForFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Free cast spends no mana from the pool")
    void freeCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's spells are not made free by your Omniscience")
    void opponentSpellsNotFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permission is gone once Omniscience leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Omniscience"));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
