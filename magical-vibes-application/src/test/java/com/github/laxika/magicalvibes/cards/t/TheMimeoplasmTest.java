package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfExtinction;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMimeoplasm.class, GrizzlyBears.class, AirElemental.class, LordOfExtinction.class,
        Mulldrifter.class, ScavengingOoze.class, Triskelion.class, Clone.class})
class TheMimeoplasmTest extends BaseCardTest {

    @Test
    void exilesTwoCreatureCardsCopiesOneAndUsesOtherPowerForCounters() {
        Card bears = new GrizzlyBears();
        Card elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(elemental));
        castMimeoplasm();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice pairChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(pairChoice).isNotNull();
        assertThat(pairChoice.minCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elemental.getId()));

        PendingInteraction.MultiGraveyardChoice copyChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(copyChoice).isNotNull();
        assertThat(copyChoice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elemental.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent mimeoplasm = findPermanent(player1, "Grizzly Bears");
        assertThat(mimeoplasm).isNotNull();
        assertThat(mimeoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(mimeoplasm.getEffectivePower()).isEqualTo(6);
        assertThat(mimeoplasm.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elemental);
    }

    @Test
    void mayDeclineToExileCardsAndCopy() {
        Card bears = new GrizzlyBears();
        Card elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(elemental));
        castMimeoplasm();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "The Mimeoplasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(elemental);
    }

    private void castMimeoplasm() {
        harness.castFromHand(player1, new TheMimeoplasm(), "{2}{B}{G}{U}");
    }

    @Test
    void usesCharacteristicDefiningPowerAfterBothCardsAreExiled() {
        Card ooze = new ScavengingOoze();
        Card lord = new LordOfExtinction();
        Card remainingOwnCard = new Mulldrifter();
        Card remainingOpponentCard = new Triskelion();
        harness.setGraveyard(player1, List.of(ooze, lord, remainingOwnCard));
        harness.setGraveyard(player2, List.of(remainingOpponentCard));
        chooseCopy(ooze, lord);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copy.getEffectivePower()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remainingOwnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remainingOpponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(ooze, lord);
    }

    @Test
    void copiedEntryCountersAreAddedToMimeoplasmCounters() {
        Card triskelion = new Triskelion();
        Card ooze = new ScavengingOoze();
        harness.setGraveyard(player1, List.of(triskelion, ooze));
        chooseCopy(triskelion, ooze);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(copy.getEffectivePower()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(triskelion, ooze);
    }

    @Test
    void copiedEnterTriggerDrawsCardsWithoutEvokeSacrifice() {
        Card mulldrifter = new Mulldrifter();
        Card ooze = new ScavengingOoze();
        Card firstDraw = new ScavengingOoze();
        Card secondDraw = new Triskelion();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setGraveyard(player2, List.of(mulldrifter, ooze));
        chooseCopy(mulldrifter, ooze);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        harness.assertOnBattlefield(player1, "Mulldrifter");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(mulldrifter, ooze);
    }

    @Test
    void cannotCopyWhenOnlyOneCreatureCardIsAvailable() {
        Card ooze = new ScavengingOoze();
        harness.setGraveyard(player1, List.of(ooze));
        castMimeoplasm();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Mimeoplasm");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ooze);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void rejectsSelectingOneCardOrSelectingTheSameCardTwice() {
        Card ooze = new ScavengingOoze();
        Card triskelion = new Triskelion();
        harness.setGraveyard(player1, List.of(ooze, triskelion));
        castMimeoplasm();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ooze.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ooze.getId(), ooze.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ooze, triskelion);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(ooze.getId(), triskelion.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(ooze.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void chooseCopy(Card copiedCard, Card otherCard) {
        castMimeoplasm();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(copiedCard.getId(), otherCard.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(copiedCard.getId()));
    }

    @Test
    void copiedCloneCanApplyItsOwnCopyReplacementAndKeepAdditionalCounters() {
        Card clone = new Clone();
        Card ooze = new ScavengingOoze();
        harness.addToBattlefield(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(clone, ooze));
        chooseCopy(clone, ooze);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Air Elemental"));

        Permanent copy = findPermanent(player1, "Air Elemental");
        assertThat(copy).isNotNull();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copy.getEffectivePower()).isEqualTo(6);
        assertThat(copy.getEffectiveToughness()).isEqualTo(6);
    }
}
