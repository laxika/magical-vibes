package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
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

@CardUsed({DreampodDruid.class, HolyStrength.class})
class DreampodDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Saproling during each player's upkeep while enchanted")
    void createsSaprolingDuringEachUpkeepWhileEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Dreampod Druid"));
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create a Saproling when it is not enchanted")
    void doesNotCreateSaprolingWhenNotEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }
}
