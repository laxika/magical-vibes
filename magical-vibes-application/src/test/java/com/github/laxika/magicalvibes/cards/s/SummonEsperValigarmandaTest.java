package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonEsperValigarmanda.class, DarkRitual.class, GrizzlyBears.class})
class SummonEsperValigarmandaTest extends BaseCardTest {

    @Test
    void chapterIExilesAnInstantOrSorceryFromEachGraveyard() {
        DarkRitual ownRitual = new DarkRitual();
        DarkRitual opposingRitual = new DarkRitual();
        GrizzlyBears opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownRitual));
        harness.setGraveyard(player2, List.of(opposingRitual, opposingCreature));

        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperValigarmanda());
        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownRitual.getId(), opposingRitual.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownRitual.getId(), opposingRitual.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.getCardsExiledByPermanent(saga.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownRitual.getId(), opposingRitual.getId());
    }

    @Test
    void chapterIIAddsLoreManaAndCastsAnExiledSpellWithAnyMana() {
        Permanent saga = addSagaWithLore(1);
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, saga.getId());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == ritual);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ritual);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(ritual);
        assertThat(gd.findExiledCard(ritual.getId())).isNull();
    }

    @Test
    void chapterIIIAddsManaEqualToItsLoreCounters() {
        addSagaWithLore(2);

        advanceToNextChapter();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperValigarmanda());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
