package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.Plummet;
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

@CardUsed({HornetQueen.class, Plummet.class})
class HornetQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hornet Queen puts its ETB token trigger on the stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new HornetQueen()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hornet Queen");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB trigger creates four 1/1 Insect tokens with flying and deathtouch")
    void etbCreatesFourInsectTokens() {
        harness.setHand(player1, List.of(new HornetQueen()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> tokens = findPermanents(player1, "Insect");
        assertThat(tokens).hasSize(4);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.DEATHTOUCH);
        });
    }

    @Test
    @DisplayName("Insect tokens are green creatures that enter untapped and summoning sick")
    void tokensHaveCorrectCharacteristicsAndEntryState() {
        harness.setHand(player1, List.of(new HornetQueen()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(4).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        assertThat(findPermanents(player2, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("Hornet Queen creates tokens for its controller when the other player casts it")
    void otherPlayerReceivesTokens() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HornetQueen()));
        harness.addMana(player2, ManaColor.GREEN, 7);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Insect")).hasSize(4);
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("The token trigger resolves even if Hornet Queen is destroyed in response")
    void tokensCreatedAfterQueenIsDestroyed() {
        harness.setHand(player1, List.of(new HornetQueen()));
        harness.setHand(player2, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Hornet Queen").getId());

        harness.assertNotOnBattlefield(player1, "Hornet Queen");
        harness.assertInGraveyard(player1, "Hornet Queen");
        assertThat(findPermanents(player1, "Insect")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(4);
        assertThat(findPermanents(player2, "Insect")).isEmpty();
    }
}
