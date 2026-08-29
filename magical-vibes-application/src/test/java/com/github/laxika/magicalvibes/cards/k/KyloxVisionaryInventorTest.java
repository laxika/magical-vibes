package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyloxVisionaryInventor.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
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
}
