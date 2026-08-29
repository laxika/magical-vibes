package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HellcarverDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage sacrifices other permanents, discards the hand, and exiles the top six")
    void combatDamageSacrificesAndExilesTopSix() {
        Card handCard = new Forest();
        HellcarverDemon demon = new HellcarverDemon();
        Permanent source = addCreatureReady(player1, demon);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Shock shock = new Shock();
        List<Card> library = List.of(
                shock, new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, library);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(handCard, otherCreature.getOriginalCard(), otherLand.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(library.subList(0, 6).stream().map(Card::getId).toArray(UUID[]::new));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(6));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(((PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction()).validCardIds())
                .containsExactly(shock.getId());
    }

    @Test
    @DisplayName("May cast a spell from the exiled cards without paying its mana cost")
    void mayCastExiledSpellWithoutPayingMana() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                bears, new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new HellcarverDemon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears
                && entry.getControllerId().equals(player1.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).doesNotContain(bears);
    }
}
