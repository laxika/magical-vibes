package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiseOfTheDreadMarnTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie Berserker for each nontoken creature that died this turn")
    void createsZombieBerserkersForAllNontokenCreatureDeaths() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.nontokenCreatureDeathCountThisTurn.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new RiseOfTheDreadMarn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Zombie Berserker");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.ZOMBIE, CardSubtype.BERSERKER);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Creates no tokens when no nontoken creature died this turn")
    void createsNoTokensWithoutNontokenCreatureDeaths() {
        harness.setHand(player1, List.of(new RiseOfTheDreadMarn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).isEmpty();
    }

    @Test
    @DisplayName("Can be cast from exile for its foretell cost")
    void canBeCastForForetellCost() {
        RiseOfTheDreadMarn card = new RiseOfTheDreadMarn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, card.getId(), 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Berserker")).hasSize(1);
    }
}
