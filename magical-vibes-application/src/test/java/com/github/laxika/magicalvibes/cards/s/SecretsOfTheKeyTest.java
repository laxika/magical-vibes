package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SecretsOfTheKey.class)
class SecretsOfTheKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates once when cast from hand")
    void investigatesOnceFromHand() {
        harness.setHand(player1, List.of(new SecretsOfTheKey()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates twice when cast with flashback")
    void investigatesTwiceWithFlashback() {
        SecretsOfTheKey secrets = new SecretsOfTheKey();
        harness.setGraveyard(player1, List.of(secrets));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secrets);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(secrets);
    }
}
