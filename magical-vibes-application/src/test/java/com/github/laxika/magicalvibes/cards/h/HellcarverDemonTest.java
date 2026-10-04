package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OgreSentry;
import com.github.laxika.magicalvibes.cards.p.PawnOfUlamog;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellcarverDemon.class, Forest.class, GrizzlyBears.class, Shock.class,
        OgreSentry.class, PawnOfUlamog.class})
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

    @Test
    void mayDeclineAllSpellsWithFewerThanSixCardsInLibrary() {
        OgreSentry spell = new OgreSentry();
        Forest land = new Forest();
        Permanent source = addCreatureReady(player1, new HellcarverDemon());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        Forest opponentHandCard = new Forest();
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setLibrary(player1, List.of(spell, land));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentPermanent);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayCastMultipleCreatureSpellsInChosenOrderDuringCombat() {
        OgreSentry first = new OgreSentry();
        OgreSentry second = new OgreSentry();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(first, second, land));
        addCreatureReady(player1, new HellcarverDemon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(second, first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getOriginalCard).contains(first).doesNotContain(second);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getOriginalCard).contains(first, second);
    }

    @Test
    void exilesOnlyLandsWithoutOfferingToPlayThem() {
        Permanent source = addCreatureReady(player1, new HellcarverDemon());
        Forest handCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        List<Card> lands = List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, lands);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(lands);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(handCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneouslySacrificedPawnSeesOtherCreatureDie() {
        addCreatureReady(player1, new HellcarverDemon());
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new OgreSentry());
        OgreSentry exiledSpell = new OgreSentry();
        harness.setLibrary(player1, List.of(exiledSpell));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }
}
