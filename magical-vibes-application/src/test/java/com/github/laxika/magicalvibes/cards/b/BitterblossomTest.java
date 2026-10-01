package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Bitterblossom.class)
class BitterblossomTest extends BaseCardTest {

    // "At the beginning of your upkeep, you lose 1 life and create a 1/1 black Faerie Rogue creature token with flying."

    @Test
    @DisplayName("Controller loses 1 life and creates a flying Faerie Rogue token at upkeep")
    void losesLifeAndCreatesTokenAtUpkeep() {
        harness.addToBattlefield(player1, new Bitterblossom());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve life loss
        harness.passBothPriorities(); // resolve token creation

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Faerie Rogue".equals(p.getCard().getName()))
                .filter(p -> p.getCard().getKeywords().contains(Keyword.FLYING))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Life loss and token creation resolve together as one upkeep ability")
    void resolvesLifeLossAndTokenCreationAsOneAbility() {
        harness.addToBattlefield(player1, new Bitterblossom());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Faerie Rogue".equals(p.getCard().getName()))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates the printed black 1/1 Faerie Rogue creature token")
    void createsCorrectFaerieRogueToken() {
        harness.addToBattlefield(player1, new Bitterblossom());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Faerie Rogue");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE, CardSubtype.ROGUE);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.FLYING);
    }

    @Test
    @DisplayName("Faerie Rogue token prints its keywords in its text box")
    void tokenPrintsItsKeywords() {
        harness.addToBattlefield(player1, new Bitterblossom());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve life loss
        harness.passBothPriorities(); // resolve token creation

        // A token has no oracle text, so without this the flying would render nowhere
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .map(p -> p.getCard().getCardText()))
                .containsExactly("Flying");
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new Bitterblossom());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().isToken())).isFalse();
    }
}
