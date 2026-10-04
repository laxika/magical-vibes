package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({ErraticExplosion.class, Forest.class, GlorySeeker.class, Island.class, Shock.class})
class ErraticExplosionTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToFirstNonlandManaValueAndBottomsAllRevealedCards() {
        Card forest = new Forest();
        Card glorySeeker = new GlorySeeker();
        harness.setLibrary(player1, List.of(forest, glorySeeker));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(glorySeeker), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(glorySeeker, forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(glorySeeker, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canDealDamageToACreatureAndBottomASingleRevealedCard() {
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        Card revealed = new ErraticExplosion();
        harness.setLibrary(player1, List.of(revealed));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsAllCardsOnBottomWithoutDamageWhenNoNonlandIsRevealed() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void stopsAtFirstNonlandAndPutsRevealedCardsBelowUnrevealedCards() {
        Card forest = new Forest();
        Card firstNonland = new GlorySeeker();
        Card unrevealedNonland = new ErraticExplosion();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, firstNonland, unrevealedNonland, island));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactly(forest, firstNonland);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(firstNonland), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unrevealedNonland, island, firstNonland, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Erratic Explosion");
    }

    @Test
    void doesNotRevealOrReorderWhenItsTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        Card forest = new Forest();
        Card nonland = new GlorySeeker();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, nonland, island));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, nonland, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Erratic Explosion");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
