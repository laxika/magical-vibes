package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporeSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 green Saproling creature tokens")
    void createsThreeSaprolingTokens() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SporeSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);

        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(3);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SporeSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spore Swarm"));
    }
}
