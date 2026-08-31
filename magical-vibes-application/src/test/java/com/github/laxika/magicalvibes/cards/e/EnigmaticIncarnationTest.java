package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NessianHornbeetle;
import com.github.laxika.magicalvibes.cards.t.TectonicGiant;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnigmaticIncarnation.class, UnderworldDreams.class, TectonicGiant.class,
        NessianHornbeetle.class})
class EnigmaticIncarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another enchantment finds a creature with one higher mana value")
    void sacrificesAnotherEnchantmentAndFindsCreature() {
        EnigmaticIncarnation incarnation = new EnigmaticIncarnation();
        UnderworldDreams enchantment = new UnderworldDreams();
        TectonicGiant foundCreature = new TectonicGiant();
        NessianHornbeetle wrongManaValue = new NessianHornbeetle();

        harness.addToBattlefield(player1, incarnation);
        harness.addToBattlefield(player1, enchantment);
        harness.setLibrary(player1, List.of(foundCreature, wrongManaValue));

        Permanent enchantmentPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == enchantment)
                .findFirst()
                .orElseThrow();

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, enchantmentPermanent.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(foundCreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(incarnation, foundCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
    }

    @Test
    @DisplayName("Declining the trigger does not sacrifice an enchantment")
    void decliningDoesNothing() {
        EnigmaticIncarnation incarnation = new EnigmaticIncarnation();
        UnderworldDreams enchantment = new UnderworldDreams();
        harness.addToBattlefield(player1, incarnation);
        harness.addToBattlefield(player1, enchantment);

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).containsExactly(incarnation, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting without another enchantment does nothing")
    void acceptingWithoutAnotherEnchantmentDoesNothing() {
        EnigmaticIncarnation incarnation = new EnigmaticIncarnation();
        harness.addToBattlefield(player1, incarnation);
        harness.setLibrary(player1, List.of());

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).containsExactly(incarnation);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
