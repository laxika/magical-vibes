package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Morningtide.class, GrizzlyBears.class, Shock.class, DeepAnalysis.class, TerohsFaithful.class})
class MorningtideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all graveyards, then goes to its owner's graveyard")
    void exilesAllGraveyards() {
        harness.setHand(player1, List.of(new Morningtide()));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Morningtide");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves with both graveyards empty without exiling itself")
    void resolvesWithEmptyGraveyards() {
        Morningtide spell = new Morningtide();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles every card of different types while the caster's graveyard is empty")
    void exilesMultipleCardsFromOnlyNonemptyGraveyard() {
        Morningtide spell = new Morningtide();
        DeepAnalysis sorcery = new DeepAnalysis();
        TerohsFaithful creature = new TerohsFaithful();
        Morningtide otherSorcery = new Morningtide();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(sorcery, creature, otherSorcery));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(sorcery, creature, otherSorcery);
    }

    @Test
    @DisplayName("Includes cards put into graveyards after casting but before resolution")
    void usesGraveyardsAtResolution() {
        Morningtide spell = new Morningtide();
        DeepAnalysis ownCard = new DeepAnalysis();
        TerohsFaithful opponentCard = new TerohsFaithful();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }
}
