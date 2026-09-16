package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SophinaSpearsageDeserter.class, GrizzlyBears.class})
class SophinaSpearsageDeserterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with nontoken creatures creates that many Clues")
    void investigatesForEachNontokenAttackingCreature() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, createTokenCreature());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Sophina investigates for itself when it attacks alone")
    void investigatesForSophinaAlone() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private Card createTokenCreature() {
        Card token = new Card();
        token.setName("Test Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
