package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CrimsonCowlMasterOfEvil.class, GrizzlyBears.class})
class CrimsonCowlMasterOfEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 2/1 menace Villain token when one or more nontoken Villains attack a player")
    void createsVillainTokenForNontokenVillainAttackers() {
        addCreatureReady(player1, new CrimsonCowlMasterOfEvil());
        Permanent firstVillain = addCreatureReady(player1, villain(false));
        Permanent secondVillain = addCreatureReady(player1, villain(false));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstVillain),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondVillain)));
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a token Villain attacking")
    void tokenVillainAttackerDoesNotTrigger() {
        addCreatureReady(player1, new CrimsonCowlMasterOfEvil());
        Permanent tokenVillain = addCreatureReady(player1, villain(true));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(tokenVillain)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a nontoken non-Villain attacking")
    void nonVillainAttackerDoesNotTrigger() {
        addCreatureReady(player1, new CrimsonCowlMasterOfEvil());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Villain")).noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Card villain(boolean token) {
        Card card = new Card();
        card.setName("Villain");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setSubtypes(List.of(CardSubtype.VILLAIN));
        card.setPower(2);
        card.setToughness(1);
        card.setToken(token);
        return card;
    }
}
