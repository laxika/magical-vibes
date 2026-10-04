package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GoblinInstigator.class})
class GoblinInstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Instigator enters, it creates one Goblin token")
    void etbCreatesGoblinToken() {
        castAndResolve();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("Goblin token is a 1/1 red Goblin creature token")
    void tokenHasPrintedCharacteristics() {
        castAndResolve();

        Permanent token = findPermanent(player1, "Goblin");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The Goblin token is created only when the enter trigger resolves")
    void tokenWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new GoblinInstigator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Instigator")).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin Instigator creates the token for that opponent")
    void opponentControlsCreatedToken() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GoblinInstigator()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Goblin")).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GoblinInstigator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
