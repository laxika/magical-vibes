package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JoinTheDanceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Join the Dance creates two 1/1 white Human tokens")
    void createsTwoHumans() {
        harness.setHand(player1, List.of(new JoinTheDance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> humans = findPermanents(player1, "Human");

        assertThat(humans).hasSize(2);
        for (Permanent human : humans) {
            assertThat(human.getCard().getPower()).isEqualTo(1);
            assertThat(human.getCard().getToughness()).isEqualTo(1);
            assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
        }

        harness.assertInGraveyard(player1, "Join the Dance");
    }

    @Test
    @DisplayName("Flashback creates two Human tokens and exiles the card")
    void flashbackCreatesTokensAndExiles() {
        harness.setGraveyard(player1, List.of(new JoinTheDance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human")).hasSize(2);

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Join the Dance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Join the Dance"));
    }
}
