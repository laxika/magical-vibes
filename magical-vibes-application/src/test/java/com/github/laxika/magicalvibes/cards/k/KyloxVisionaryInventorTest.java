package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DemandAnswers;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyloxVisionaryInventor.class, DemandAnswers.class, Divination.class, Forest.class, GravestoneStrider.class, GrizzlyBears.class, Shock.class})
class KyloxVisionaryInventorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices any number of other creatures and exiles cards equal to their total power")
    void sacrificesCreaturesAndExilesByTotalPower() {
        Permanent kylox = addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Divination divination = new Divination();
        Shock shock = new Shock();
        GrizzlyBears libraryCreature = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(divination, shock, libraryCreature, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.validIds()).doesNotContain(kylox.getId(), land.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kylox, secondCreature, land);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(divination.getId(), shock.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature, forest);
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(divination.getId(), shock.getId());
        assertThat(castChoice.validCardIds()).doesNotContain(libraryCreature.getId(), forest.getId());
    }

    @Test
    @DisplayName("Casts selected exiled instants and sorceries without paying their mana costs")
    void castsSelectedSpellWithoutPayingMana() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Divination divination = new Divination();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(divination, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == divination
                && entry.getControllerId().equals(player1.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(divination.getId())).isNull();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exiles no cards when no other creatures are sacrificed")
    void noOtherCreaturesMeansNoExile() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May sacrifice zero creatures even when other creatures are available")
    void mayDeclineSacrifices() {
        Permanent kylox = addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kylox, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Adds the effective power of all sacrificed creatures and filters the exiled cards")
    void sumsEffectivePowerAndLeavesUncastCardsExiled() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Divination divination = new Divination();
        Shock shock = new Shock();
        GrizzlyBears libraryCreature = new GrizzlyBears();
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        Forest remainingLand = new Forest();
        harness.setLibrary(player1, List.of(divination, shock, libraryCreature,
                firstLand, secondLand, remainingLand));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).doesNotContain(opposingCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(divination.getId(), shock.getId(), libraryCreature.getId(),
                        firstLand.getId(), secondLand.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(divination.getId(), shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == divination || entry.getCard() == shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casts multiple selected spells in the chosen order, including a targeted instant")
    void castsMultipleSpellsInChosenOrder() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Divination divination = new Divination();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(divination, shock, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId(), shock.getId()));
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(divination, shock);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(divination.getId())).isNull();
        assertThat(gd.findExiledCard(shock.getId())).isNull();
    }

    @Test
    @DisplayName("Exiles only the available library cards when total sacrificed power is larger")
    void shortLibraryDoesNotPreventExile() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(forest.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({KyloxVisionaryInventor.class, GravestoneStrider.class, DemandAnswers.class, Forest.class})
    @DisplayName("Allows payment of a required additional cost for an exiled spell")
    void offersAdditionalCostPaymentForDemandAnswers() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        DemandAnswers demandAnswers = new DemandAnswers();
        Forest discardCard = new Forest();
        harness.setHand(player1, List.of(discardCard));
        harness.setLibrary(player1, List.of(demandAnswers, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(demandAnswers.getId()));

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardCard);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == demandAnswers);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Negative power reduces the total power of the sacrificed creatures")
    void includesNegativePowerInTotal() {
        addCreatureReady(player1, new KyloxVisionaryInventor());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setPowerModifier(-3);
        Forest exiledLand = new Forest();
        Forest remainingLand = new Forest();
        harness.setLibrary(player1, List.of(exiledLand, remainingLand));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(exiledLand.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
