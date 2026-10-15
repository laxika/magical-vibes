package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        assertThat(findPermanents(player1, "Goblin Token")).hasSize(2);
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
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.getCardsExiledByPermanent(neriv.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void commanderAttackAllowsPlayingThisTurnsExiledCards() {
        Permanent neriv = enterNerivWithTokens();
        gd.makeCommander(player1.getId(), neriv.getCard());
        Card exiledCard = new Forest();
        harness.setLibrary(player1, List.of(exiledCard));

        int nerivIndex = gd.playerBattlefields.get(player1.getId()).indexOf(neriv);
        declareAttackers(List.of(nerivIndex));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

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
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cardsRemainPlayableAfterNerivLeaves() {
        Permanent neriv = enterNerivWithTokens();
        gd.makeCommander(player1.getId(), neriv.getCard());
        Card exiledCard = new Forest();
        harness.setLibrary(player1, List.of(exiledCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, neriv));

        harness.castFromExile(player1, exiledCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    void cardsExiledOnAnEarlierTurnRemainPlayableAfterCommanderAttacks() {
        Permanent neriv = enterNerivWithTokens();
        gd.makeCommander(player1.getId(), neriv.getCard());
        Card exiledCard = new Forest();
        harness.setLibrary(player1, List.of(exiledCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, exiledCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    void exiledSpellsRequireTheirNormalManaCost() {
        Permanent neriv = enterNerivWithTokens();
        gd.makeCommander(player1.getId(), neriv.getCard());
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    void anotherGoblinTokenDoesNotIncreaseTheDistinctNameCount() {
        Permanent neriv = enterNerivWithTokens();
        addToken("Goblin Token");
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(neriv)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.getCardsExiledByPermanent(neriv.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
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
