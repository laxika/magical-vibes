package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdelothTheAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Saprolings and other Treefolk get +1/+1 regardless of controller")
    void buffsSaprolingsAndOtherTreefolk() {
        Permanent ownSaproling = harness.addToBattlefieldAndReturn(player1,
                creature("Saproling", CardSubtype.SAPROLING, 1, 1));
        Permanent ownTreefolk = harness.addToBattlefieldAndReturn(player1,
                creature("Treefolk", CardSubtype.TREEFOLK, 2, 3));
        Permanent opponentTreefolk = harness.addToBattlefieldAndReturn(player2,
                creature("Opponent Treefolk", CardSubtype.TREEFOLK, 3, 4));
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2,
                creature("Unrelated Creature", CardSubtype.BEAR, 2, 2));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VerdelothTheAncient());

        assertThat(gqs.getEffectivePower(gd, ownSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownTreefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownTreefolk)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentTreefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentTreefolk)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, unrelated)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unrelated)).isEqualTo(2);

        int sourcePower = source.getCard().getPower();
        int sourceToughness = source.getCard().getToughness();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(sourcePower);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(sourceToughness);
    }

    @Test
    @DisplayName("An un-kicked Verdeloth creates no Saproling tokens")
    void unKickedCreatesNoTokens() {
        harness.setHand(player1, List.of(new VerdelothTheAncient()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Kicker X pays X and creates X Saproling tokens")
    void kickedCreatesXTokensAndPaysX() {
        harness.setHand(player1, List.of(new VerdelothTheAncient()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }

    private Card creature(String name, CardSubtype subtype, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
