package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EllieBrickMaster.class, GrizzlyBears.class, GiantGrowth.class})
class EllieBrickMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking an opponent creates a tapped and attacking Cordyceps Infected token")
    void attackingAnOpponentCreatesTokenForAttackingPlayer() {
        addCreatureReady(player1, new EllieBrickMaster());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Cordyceps Infected").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.FUNGUS, CardSubtype.ZOMBIE);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Attacking Ellie does not create a token")
    void attackingEllieDoesNotCreateToken() {
        addCreatureReady(player1, new EllieBrickMaster());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cordyceps Infected")).isEmpty();
        assertThat(findPermanents(player2, "Cordyceps Infected")).isEmpty();
    }
}
