package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConclaveCavalier.class, WrathOfGod.class})
class ConclaveCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("When Conclave Cavalier dies, it creates two 2/2 green and white Elf Knight tokens with vigilance")
    void deathTriggerCreatesElfKnightTokens() {
        harness.addToBattlefield(player1, new ConclaveCavalier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Elf Knight");
        assertThat(tokens).hasSize(2);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.KNIGHT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
            assertThat(token.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Each Cavalier dying simultaneously creates its own pair of tokens")
    void simultaneousDeathsCreateFourTokens() {
        harness.addToBattlefield(player1, new ConclaveCavalier());
        harness.addToBattlefield(player1, new ConclaveCavalier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Knight")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elf Knight")).hasSize(4);
        assertThat(findPermanents(player1, "Conclave Cavalier")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's dying Cavalier creates tokens for that opponent")
    void opponentReceivesTheirDeathTriggerTokens() {
        harness.addToBattlefield(player2, new ConclaveCavalier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elf Knight")).isEmpty();
        assertThat(findPermanents(player2, "Elf Knight")).hasSize(2);
    }

    @Test
    @DisplayName("Conclave Cavalier attacks without tapping or creating tokens")
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent cavalier = addCreatureReady(player1, new ConclaveCavalier());

        declareAttackers(List.of(0));

        assertThat(cavalier.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Elf Knight")).isEmpty();
    }
}
