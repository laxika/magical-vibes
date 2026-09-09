package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EagerGlyphmageTest extends BaseCardTest {

    @Test
    @DisplayName("Eager Glyphmage creates a 1/1 flying Inkling token when it enters")
    void etbCreatesFlyingInklingToken() {
        harness.setHand(player1, List.of(new EagerGlyphmage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(1);
        Permanent inkling = inklings.getFirst();
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }
}
