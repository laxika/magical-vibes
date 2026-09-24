package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EmptyTheWarrens;
import com.github.laxika.magicalvibes.cards.g.GalvanicRelay;
import com.github.laxika.magicalvibes.cards.g.Grapeshot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargedConjuration.class, EmptyTheWarrens.class, GalvanicRelay.class,
        Grapeshot.class, GrizzlyBears.class})
class ChargedConjurationTest extends BaseCardTest {

    @Test
    void upkeepMakesInstantAndSorceryInHandCostOneLess() {
        addCreatureReady(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new EmptyTheWarrens(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    @Test
    void sacrificeOffersSpellbookCardAndConjuresSelectedCard() {
        addCreatureReady(player1, new ChargedConjuration());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        UUID selectedId = choice.validCardIds().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selectedId));

        assertThat(countPermanents(player1, "Charged Conjuration")).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Empty the Warrens");
    }
}
