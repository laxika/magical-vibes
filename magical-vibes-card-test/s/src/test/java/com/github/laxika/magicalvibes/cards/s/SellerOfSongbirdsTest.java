package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DramaticRescue;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SellerOfSongbirds.class, DramaticRescue.class})
class SellerOfSongbirdsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Bird token with flying")
    void etbCreatesBirdToken() {
        harness.castFromHand(player1, new SellerOfSongbirds(), "{2}{W}");
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
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SellerOfSongbirds(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Bird")).hasSize(1);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("The Bird is created only when the enter trigger resolves")
    void birdWaitsForTriggerResolution() {
        harness.castFromHand(player1, new SellerOfSongbirds(), "{2}{W}");
        assertThat(findPermanents(player1, "Bird")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Seller of Songbirds")).hasSize(1);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Seller in response does not stop its trigger, and recasting creates another Bird")
    void triggerSurvivesRemovalAndTriggersOnReentry() {
        harness.castFromHand(player1, new SellerOfSongbirds(), "{2}{W}");
        harness.passBothPriorities();
        Permanent seller = findPermanent(player1, "Seller of Songbirds");

        harness.setHand(player2, List.of(new DramaticRescue()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, seller.getId());

        assertThat(findPermanents(player1, "Seller of Songbirds")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(seller.getCard());
        assertThat(findPermanents(player1, "Bird")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        assertThat(findPermanents(player2, "Bird")).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Seller of Songbirds")).hasSize(1);
        assertThat(findPermanents(player1, "Bird")).hasSize(2);
    }
}
