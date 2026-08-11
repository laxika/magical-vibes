package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElderAuntieTest extends BaseCardTest {

    @Test
    void enteringCreatesABlackAndRedGoblinToken() {
        harness.setHand(player1, List.of(new ElderAuntie()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(1);
        Permanent goblin = goblins.getFirst();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.getCard().isToken()).isTrue();
    }
}
