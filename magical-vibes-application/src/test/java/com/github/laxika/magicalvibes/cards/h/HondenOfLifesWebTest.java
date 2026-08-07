package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HondenOfLifesWebTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Spirit token for each Shrine its controller controls")
    void createsSpiritForEachControlledShrine() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player1, shrine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> spirits = spiritTokens();
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Counts Shrines when the upkeep trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, shrine());
        harness.passBothPriorities();

        assertThat(spiritTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player2, shrine());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(spiritTokens()).isEmpty();
    }

    private List<Permanent> spiritTokens() {
        return findPermanents(player1, "Spirit");
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }
}
