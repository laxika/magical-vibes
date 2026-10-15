package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomeTheDead.class, Millstone.class, Forest.class, Island.class, Plains.class,
        GrizzlyBears.class})
class WelcomeTheDeadTest extends BaseCardTest {

    @Test
    void drawsDiscardsLosesLifeAndCreatesTappedZombiesForHandAndLibraryCards() {
        Card firstMilled = new Forest();
        Card secondMilled = new Island();
        Card firstDraw = new Plains();
        Card secondDraw = new Plains();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(
                new Millstone(), new WelcomeTheDead(), discarded)));
        harness.setLibrary(player1, List.of(firstMilled, secondMilled, firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent millstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Millstone)
                .findFirst()
                .orElseThrow();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                null, player1.getId());
        harness.passBothPriorities();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstMilled, secondMilled, discarded);
    }

    @Test
    void flashbackUsesTheSameEffectsAndExilesTheSpell() {
        GrizzlyBears discarded = new GrizzlyBears();
        WelcomeTheDead card = new WelcomeTheDead();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setGraveyard(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void countsTheMilledSpellEvenAfterItLeavesTheGraveyardForFlashback() {
        WelcomeTheDead card = new WelcomeTheDead();
        Forest milledLand = new Forest();
        harness.setHand(player1, List.of(new Millstone()));
        harness.setLibrary(player1, List.of(card, milledLand, new Island(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(3)
                .allMatch(Permanent::isTapped);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void canDiscardADrawnCardWhenCastingWithNoOtherCardsInHand() {
        WelcomeTheDead card = new WelcomeTheDead();
        Forest discarded = new Forest();
        Island kept = new Island();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(discarded, kept));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, card);
        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
    }
}
