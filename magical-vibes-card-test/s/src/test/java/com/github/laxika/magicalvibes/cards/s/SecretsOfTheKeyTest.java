package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretsOfTheKey.class})
class SecretsOfTheKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates once when cast from hand")
    void investigatesOnceFromHand() {
        harness.setHand(player1, List.of(new SecretsOfTheKey()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates twice when cast with flashback")
    void investigatesTwiceWithFlashback() {
        SecretsOfTheKey secrets = new SecretsOfTheKey();
        harness.setGraveyard(player1, List.of(secrets));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secrets);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(secrets);
    }

    @Test
    @DisplayName("Casting from hand then flashing back investigates three times in total")
    void handCastThenFlashbackCreatesThreeClues() {
        SecretsOfTheKey secrets = new SecretsOfTheKey();
        harness.setHand(player1, List.of(secrets));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secrets);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(secrets);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        harness.assertNotOnBattlefield(player2, "Clue");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(secrets);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secrets);
    }

    @Test
    @DisplayName("The investigated Clue is sacrificed as a cost and draws on resolution")
    void investigatedClueDrawsCard() {
        SecretsOfTheKey drawnCard = new SecretsOfTheKey();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SecretsOfTheKey()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertNotOnBattlefield(player2, "Clue");
    }
}
