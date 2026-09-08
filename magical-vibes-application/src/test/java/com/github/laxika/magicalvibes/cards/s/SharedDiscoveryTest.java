package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharedDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps four untapped creatures as an additional cost, then draws three cards")
    void tapsFourCreaturesAndDrawsThreeCards() {
        List<Permanent> creatures = addCreatures(4);
        harness.setHand(player1, List.of(new SharedDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorceryWithSacrifices(player1, 0, null,
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot cast it without four untapped creatures")
    void cannotCastWithoutFourUntappedCreatures() {
        List<Permanent> creatures = addCreatures(4);
        creatures.getFirst().tap();
        harness.setHand(player1, List.of(new SharedDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null,
                creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot tap a creature controlled by another player to pay the cost")
    void cannotTapOpponentsCreature() {
        List<Permanent> creatures = addCreatures(4);
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);
        harness.setHand(player1, List.of(new SharedDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<java.util.UUID> chosen = new ArrayList<>(creatures.stream().map(Permanent::getId).limit(3).toList());
        chosen.add(opponentCreature.getId());

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null, chosen))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    private List<Permanent> addCreatures(int count) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent creature = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(player1.getId()).add(creature);
            creatures.add(creature);
        }
        return creatures;
    }
}
