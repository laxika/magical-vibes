package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SproutSwarm.class, GrizzlyBears.class})
class SproutSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 green Saproling creature token")
    void createsSaprolingToken() {
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).singleElement().satisfies(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Paying buyback returns Sprout Swarm to hand as it resolves")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, null);
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Sprout Swarm")
    void convokeTapsCreature() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }
}
