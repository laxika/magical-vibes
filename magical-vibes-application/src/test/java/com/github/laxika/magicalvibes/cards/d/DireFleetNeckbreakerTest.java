package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetNeckbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Pirates you control get +2/+0")
    void boostsAttackingPiratesYouControl() {
        Permanent neckbreaker = addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent attackingPirate = addCreatureReady(player1, createPirateCard("Attacking Pirate"));
        Permanent stayingPirate = addCreatureReady(player1, createPirateCard("Staying Pirate"));
        Permanent nonPirate = addCreatureReady(player1, createNonPirateCard("Non-Pirate"));
        Permanent opponentPirate = addCreatureReady(player2, createPirateCard("Opponent Pirate"));

        int neckbreakerPower = gqs.getEffectivePower(gd, neckbreaker);
        int attackingPiratePower = gqs.getEffectivePower(gd, attackingPirate);
        int stayingPiratePower = gqs.getEffectivePower(gd, stayingPirate);
        int nonPiratePower = gqs.getEffectivePower(gd, nonPirate);
        int opponentPiratePower = gqs.getEffectivePower(gd, opponentPirate);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, neckbreaker)).isEqualTo(neckbreakerPower + 2);
        assertThat(gqs.getEffectivePower(gd, attackingPirate)).isEqualTo(attackingPiratePower + 2);
        assertThat(gqs.getEffectivePower(gd, stayingPirate)).isEqualTo(stayingPiratePower);
        assertThat(gqs.getEffectivePower(gd, nonPirate)).isEqualTo(nonPiratePower);
        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(opponentPiratePower);
    }

    private Card createPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.PIRATE));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createNonPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
