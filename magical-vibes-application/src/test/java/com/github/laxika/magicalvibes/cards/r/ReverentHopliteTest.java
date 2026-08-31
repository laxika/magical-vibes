package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
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

@CardUsed({ReverentHoplite.class, SavannahLions.class})
class ReverentHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates one Human Soldier for its own white mana symbol")
    void createsOneTokenFromItsOwnDevotion() {
        castReverentHoplite();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Entering the battlefield creates Human Soldiers equal to white devotion")
    void createsTokensEqualToWhiteDevotion() {
        harness.addToBattlefield(player1, new SavannahLions());

        castReverentHoplite();

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        });
    }

    private void castReverentHoplite() {
        harness.setHand(player1, List.of(new ReverentHoplite()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
