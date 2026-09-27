package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DeployToTheFront.class, GrizzlyBears.class})
class DeployToTheFrontTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Soldier for each creature on the battlefield")
    void createsSoldiersForAllCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DeployToTheFront()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Soldier");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }
}
