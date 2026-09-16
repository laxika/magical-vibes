package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({ErraticExplosion.class, Forest.class, GlorySeeker.class, Island.class})
class ErraticExplosionTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToFirstNonlandManaValueAndBottomsAllRevealedCards() {
        Card forest = new Forest();
        Card glorySeeker = new GlorySeeker();
        harness.setLibrary(player1, List.of(forest, glorySeeker));
        harness.setHand(player1, List.of(new ErraticExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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
}
