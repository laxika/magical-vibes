package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReduceToDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts and enchantments on both sides to their owners' hands")
    void returnsAllArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReduceToDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Millstone", "Glorious Anthem");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Ornithopter");
    }

    @Test
    @DisplayName("Leaves non-artifact non-enchantment permanents alone")
    void leavesOtherPermanentsAlone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, List.of(new ReduceToDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Millstone");
    }

    @Test
    @DisplayName("Resolves with nothing to bounce and goes to the graveyard")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new ReduceToDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reduce to Dreams");
    }
}
