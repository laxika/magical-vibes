package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelflessGlyphweaverTest extends BaseCardTest {

    @Test
    void exilesItselfToGiveYourCreaturesIndestructibleUntilEndOfTurn() {
        Permanent glyphweaver = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(glyphweaver.getCard());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void deadlyVanityKeepsTheChosenPlaneswalkerAndDestroysOtherCreaturesAndPlaneswalkers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent keptPlaneswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new SelflessGlyphweaver()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Exactly one permanent");

        harness.handleMultiplePermanentsChosen(player1, List.of(keptPlaneswalker.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }
}
