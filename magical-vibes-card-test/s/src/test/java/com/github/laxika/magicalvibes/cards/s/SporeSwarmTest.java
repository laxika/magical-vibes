package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporeSwarm.class})
class SporeSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 green Saproling creature tokens")
    void createsThreeSaprolingTokens() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SporeSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);

        List<Permanent> tokens = findPermanents(player1, "Saproling");
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

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Spore Swarm");
    }

    @Test
    @DisplayName("Creates tokens for the caster during the opponent's turn")
    void createsTokensForCasterDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SporeSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        gs.passPriority(gd, player2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        harness.assertInGraveyard(player1, "Spore Swarm");
    }
}
