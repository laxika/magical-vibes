package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DivineVisitationTest extends BaseCardTest {

    @Test
    void replacesCreatureTokensWithAngels() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Angel");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(token.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void doesNotReplaceOpponentsCreatureTokens() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).isEmpty();
    }
}
