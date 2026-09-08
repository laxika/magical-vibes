package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KethekCrucibleGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature reveals the first nonlegendary creature with lesser mana value")
    void sacrificesAndRevealsLowerManaValueCreature() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        AzureDrake sacrificedCard = new AzureDrake();
        GrizzlyBears remainingCreature = new GrizzlyBears();
        AzureDrake equalManaValueCard = new AzureDrake();
        FountainOfYouth noncreatureCard = new FountainOfYouth();
        LlanowarElves foundCard = new LlanowarElves();

        harness.addToBattlefield(player1, kethek);
        harness.addToBattlefield(player1, sacrificedCard);
        harness.addToBattlefield(player1, remainingCreature);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(equalManaValueCard, noncreatureCard, foundCard));

        Permanent sacrificed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == sacrificedCard)
                .findFirst()
                .orElseThrow();

        moveToEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(foundCard, remainingCreature);
        assertThat(library).containsExactlyInAnyOrder(equalManaValueCard, noncreatureCard);
    }

    @Test
    @DisplayName("Declining Kethek's trigger does not sacrifice a creature")
    void declineDoesNothing() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        AzureDrake creature = new AzureDrake();

        harness.addToBattlefield(player1, kethek);
        harness.addToBattlefield(player1, creature);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        FountainOfYouth libraryCard = new FountainOfYouth();
        library.add(libraryCard);

        moveToEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(creature);
        assertThat(library).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Accepting with no other creatures does nothing")
    void acceptWithNoOtherCreaturesDoesNothing() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        LlanowarElves libraryCard = new LlanowarElves();

        harness.addToBattlefield(player1, kethek);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.add(libraryCard);

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(kethek);
        assertThat(library).containsExactly(libraryCard);
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
