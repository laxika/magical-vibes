package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GoblinInstigator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
