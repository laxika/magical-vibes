package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndustrialAdvancement.class, GrizzlyBears.class, AirElemental.class, Shock.class})
class IndustrialAdvancementTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and looks at that many cards for a creature")
    void sacrificesCreatureAndPutsCreatureFromTopCardsOntoBattlefield() {
        IndustrialAdvancement advancement = new IndustrialAdvancement();
        GrizzlyBears sacrificed = new GrizzlyBears();
        AirElemental found = new AirElemental();
        Shock rest = new Shock();

        harness.addToBattlefield(player1, advancement);
        Permanent sacrificedPermanent = harness.addToBattlefieldAndReturn(player1, sacrificed);
        harness.setLibrary(player1, List.of(found, rest));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedPermanent.getId());

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(found.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(found.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).containsExactly(advancement, found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rest);
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the battlefield and library unchanged")
    void decliningDoesNothing() {
        IndustrialAdvancement advancement = new IndustrialAdvancement();
        GrizzlyBears sacrificed = new GrizzlyBears();
        AirElemental topCard = new AirElemental();

        harness.addToBattlefield(player1, advancement);
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(topCard));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).containsExactly(advancement, sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The sacrificed creature's mana value limits the cards looked at")
    void looksAtOnlyTheSacrificedManaValueNumberOfCards() {
        IndustrialAdvancement advancement = new IndustrialAdvancement();
        GrizzlyBears sacrificed = new GrizzlyBears();
        Shock first = new Shock();
        Shock second = new Shock();
        AirElemental third = new AirElemental();

        harness.addToBattlefield(player1, advancement);
        Permanent sacrificedPermanent = harness.addToBattlefieldAndReturn(player1, sacrificed);
        harness.setLibrary(player1, List.of(first, second, third));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedPermanent.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).containsExactly(advancement);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
