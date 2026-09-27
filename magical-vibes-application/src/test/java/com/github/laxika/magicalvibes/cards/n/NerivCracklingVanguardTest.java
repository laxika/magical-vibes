package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NerivCracklingVanguard.class, Forest.class, GrizzlyBears.class})
class NerivCracklingVanguardTest extends BaseCardTest {

    @Test
    void entersWithTwoGoblinTokens() {
        Permanent neriv = harness.enterBattlefieldAndReturn(player1, new NerivCracklingVanguard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(neriv).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void exilesOneCardPerDistinctTokenName() {
        Permanent neriv = enterNerivWithTokens();
        addToken("Treasure");
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.getCardsExiledByPermanent(neriv.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void commanderAttackAllowsPlayingThisTurnsExiledCards() {
        Permanent neriv = enterNerivWithTokens();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        Card exiledCard = new Forest();
        harness.setLibrary(player1, List.of(exiledCard));

        int nerivIndex = gd.playerBattlefields.get(player1.getId()).indexOf(neriv);
        int commanderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(commander);
        declareAttackers(List.of(nerivIndex, commanderIndex));
        harness.passUntil(player1, com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    void noncommanderAttackDoesNotAllowPlayingExiledCards() {
        Permanent neriv = enterNerivWithTokens();
        Card exiledCard = new Forest();
        harness.setLibrary(player1, List.of(exiledCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent enterNerivWithTokens() {
        Permanent neriv = harness.enterBattlefieldAndReturn(player1, new NerivCracklingVanguard());
        neriv.setSummoningSick(false);
        harness.passBothPriorities();
        return neriv;
    }

    private void addToken(String name) {
        Card tokenCard = new Card();
        tokenCard.setName(name);
        tokenCard.setType(CardType.ARTIFACT);
        tokenCard.setToken(true);
        harness.addToBattlefield(player1, tokenCard);
    }
}
