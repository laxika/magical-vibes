package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeyaliSunsVanguard.class, Shock.class, GrizzlyBears.class})
class NeyaliSunsVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking tokens get double strike and exile the top card for conditional play")
    void attackingTokensGetDoubleStrikeAndExileTopCard() {
        addCreatureReady(player1, new NeyaliSunsVanguard());
        Permanent token = addCreatureReady(player1, tokenCreature());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_STRIKE)).isFalse();

        declareAttackers(List.of(1));

        assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_STRIKE)).isTrue();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The exiled card remains playable after Neyali leaves the battlefield")
    void exiledCardRemainsPlayableAfterNeyaliLeaves() {
        Permanent neyali = addCreatureReady(player1, new NeyaliSunsVanguard());
        addCreatureReady(player1, tokenCreature());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(neyali);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Attacking with a non-token does not trigger Neyali")
    void nonTokenAttackDoesNotTrigger() {
        addCreatureReady(player1, new NeyaliSunsVanguard());
        addCreatureReady(player1, new GrizzlyBears());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
