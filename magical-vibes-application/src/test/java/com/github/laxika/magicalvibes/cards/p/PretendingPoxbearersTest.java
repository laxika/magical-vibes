package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({PretendingPoxbearers.class, Shock.class})
class PretendingPoxbearersTest extends BaseCardTest {

    @Test
    @DisplayName("When Pretending Poxbearers dies, it creates a 1/1 white Ally token")
    void deathTriggerCreatesAllyToken() {
        Permanent poxbearers = harness.addToBattlefieldAndReturn(player1, new PretendingPoxbearers());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, poxbearers.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Ally").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The death trigger creates the Ally token under the creature's controller")
    void deathTriggerBelongsToController() {
        Permanent poxbearers = harness.addToBattlefieldAndReturn(player2, new PretendingPoxbearers());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, poxbearers.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Ally")).hasSize(1);
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }
}
