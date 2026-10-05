package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SulfurousBlast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PenumbraSpider.class, SulfurousBlast.class})
class PenumbraSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("When Penumbra Spider dies, a 2/4 black Spider token with reach is created")
    void deathTriggerCreatesSpiderToken() {
        harness.addToBattlefield(player1, new PenumbraSpider());
        destroyWithSulfurousBlast();

        List<Permanent> tokens = findPermanents(player1, "Spider");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Each Penumbra Spider creates a token when multiple copies die together")
    void eachSpiderCreatesATokenWhenTheyDieTogether() {
        harness.addToBattlefield(player1, new PenumbraSpider());
        harness.addToBattlefield(player1, new PenumbraSpider());

        destroyWithSulfurousBlast();

        assertThat(findPermanents(player1, "Spider")).hasSize(2);
    }

    @Test
    @DisplayName("The creature's controller creates the token when an opponent's Penumbra Spider dies")
    void deathTriggerCreatesTokenForTheCreatureController() {
        harness.addToBattlefield(player2, new PenumbraSpider());

        destroyWithSulfurousBlast();

        assertThat(findPermanents(player2, "Spider")).hasSize(1);
        assertThat(findPermanents(player1, "Spider")).isEmpty();
    }

    private void destroyWithSulfurousBlast() {
        castAndResolveSulfurousBlast();
        castAndResolveSulfurousBlast();
    }

    @Test
    @DisplayName("Nonlethal damage does not create a Spider token")
    void nonlethalDamageDoesNotCreateToken() {
        harness.addToBattlefield(player1, new PenumbraSpider());

        castAndResolveSulfurousBlast();

        assertThat(findPermanents(player1, "Penumbra Spider")).hasSize(1);
        assertThat(findPermanents(player1, "Spider")).isEmpty();
    }

    @Test
    @DisplayName("The Spider token does not create another token when it dies")
    void tokenDoesNotInheritDeathTrigger() {
        harness.addToBattlefield(player1, new PenumbraSpider());
        destroyWithSulfurousBlast();
        assertThat(findPermanents(player1, "Spider")).hasSize(1);
        assertThat(findPermanents(player1, "Penumbra Spider")).isEmpty();

        destroyWithSulfurousBlast();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveSulfurousBlast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SulfurousBlast(), "{2}{R}{R}");
        resolveAllTriggers();
    }
}
