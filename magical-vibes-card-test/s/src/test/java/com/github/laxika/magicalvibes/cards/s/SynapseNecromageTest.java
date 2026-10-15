package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynapseNecromage.class})
class SynapseNecromageTest extends BaseCardTest {

    @Test
    @DisplayName("When Synapse Necromage dies, it creates two Fungus tokens that can't block")
    void deathCreatesFungusTokensThatCannotBlock() {
        Permanent necromage = harness.addToBattlefieldAndReturn(player1, new SynapseNecromage());
        necromage.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Fungus");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FUNGUS);
            assertThat(bls.canBlock(gd, token)).isFalse();
        });
    }

    @Test
    @DisplayName("Tokens are created when the death trigger resolves, not when the creature dies")
    void tokensWaitForDeathTriggerToResolve() {
        Permanent necromage = harness.addToBattlefieldAndReturn(player1, new SynapseNecromage());
        necromage.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Synapse Necromage")).isEmpty();
        assertThat(findPermanents(player1, "Fungus")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(2);
        assertThat(findPermanents(player2, "Fungus")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's dying Synapse Necromage creates tokens for that opponent")
    void opponentsDeathCreatesTokensForOpponent() {
        Permanent necromage = harness.addToBattlefieldAndReturn(player2, new SynapseNecromage());
        necromage.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
        assertThat(findPermanents(player2, "Fungus")).hasSize(2)
                .allSatisfy(token -> assertThat(bls.canBlock(gd, token)).isFalse());
    }

    @Test
    @DisplayName("Each Synapse Necromage that dies simultaneously creates its own two tokens")
    void simultaneousDeathsCreateFourTokens() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SynapseNecromage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SynapseNecromage());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Synapse Necromage")).isEmpty();
        assertThat(findPermanents(player1, "Fungus")).hasSize(4)
                .allSatisfy(token -> assertThat(bls.canBlock(gd, token)).isFalse());
    }
}
