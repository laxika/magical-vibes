package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SellerOfSongbirdsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Bird token with flying")
    void etbCreatesBirdToken() {
        harness.setHand(player1, List.of(new SellerOfSongbirds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve the creature spell
        harness.passBothPriorities(); // Resolve the ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Bird");
        assertThat(tokens).hasSize(1);

        Permanent bird = tokens.getFirst();
        assertThat(bird.getCard().getPower()).isEqualTo(1);
        assertThat(bird.getCard().getToughness()).isEqualTo(1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(bird.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(bird.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The token is created under the controller's control")
    void tokenGoesToController() {
        harness.setHand(player2, List.of(new SellerOfSongbirds()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Bird")).hasSize(1);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }
}
