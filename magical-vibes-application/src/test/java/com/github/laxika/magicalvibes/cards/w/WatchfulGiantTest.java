package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatchfulGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Human token")
    void enteringTheBattlefieldCreatesHumanToken() {
        harness.setHand(player1, List.of(new WatchfulGiant()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
    }
}
