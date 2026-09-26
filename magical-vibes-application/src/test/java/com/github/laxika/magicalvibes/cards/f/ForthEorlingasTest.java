package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ForthEorlingas.class)
class ForthEorlingasTest extends BaseCardTest {

    @Test
    void createsXHastyTramplingHumanKnights() {
        castForthEorlingas(3);

        List<Permanent> knights = findPermanents(player1, "Human Knight");
        assertThat(knights).hasSize(3);
        assertThat(knights).allSatisfy(knight -> {
            assertThat(knight.getCard().getPower()).isEqualTo(2);
            assertThat(knight.getCard().getToughness()).isEqualTo(2);
            assertThat(knight.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(knight.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.KNIGHT);
            assertThat(knight.getCard().getKeywords())
                    .contains(Keyword.TRAMPLE, Keyword.HASTE);
        });
    }

    @Test
    void becomesTheMonarchWhenAControlledCreatureDealsCombatDamageThisTurn() {
        castForthEorlingas(1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotBecomeTheMonarchWithoutCombatDamage() {
        castForthEorlingas(1);

        assertThat(gd.monarchPlayerId).isNull();
    }

    private void castForthEorlingas(int xValue) {
        harness.setHand(player1, List.of(new ForthEorlingas()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
