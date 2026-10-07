package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeonardoWorldlyWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplinterAgingChampion.class, LeonardoWorldlyWarrior.class})
class SplinterAgingChampionTest extends BaseCardTest {

    @Test
    @DisplayName("When Splinter enters, it destroys a tapped creature")
    void entersAndDestroysTappedCreature() {
        Permanent target = addCreatureReady(player2, new LeonardoWorldlyWarrior());
        target.tap();

        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonardo, Worldly Warrior");
        harness.assertInGraveyard(player2, "Leonardo, Worldly Warrior");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = addCreatureReady(player2, new LeonardoWorldlyWarrior());

        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("When Splinter leaves, its controller and an opponent each draw a card")
    void leavesAndBothPlayersDraw() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterAgingChampion());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.setLibrary(player2, List.of(new LeonardoWorldlyWarrior()));

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, splinter));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Leonardo, Worldly Warrior");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Leonardo, Worldly Warrior");
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability cannot target its controller")
    void leavesCannotTargetController() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterAgingChampion());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, splinter));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutChoosingAnAvailableTappedCreature() {
        Permanent target = addCreatureReady(player2, new LeonardoWorldlyWarrior());
        target.tap();
        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.assertOnBattlefield(player1, "Splinter, Aging Champion");
        harness.assertOnBattlefield(player2, "Leonardo, Worldly Warrior");
        harness.assertNotInGraveyard(player2, "Leonardo, Worldly Warrior");
    }

    @Test
    void canEnterWhenThereAreNoTappedCreatures() {
        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Splinter, Aging Champion");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyItsControllersTappedCreature() {
        Permanent target = addCreatureReady(player1, new LeonardoWorldlyWarrior());
        target.tap();
        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Leonardo, Worldly Warrior");
        harness.assertInGraveyard(player1, "Leonardo, Worldly Warrior");
    }

    @Test
    void doesNotDestroyCreatureThatUntapsBeforeTriggerResolves() {
        Permanent target = addCreatureReady(player2, new LeonardoWorldlyWarrior());
        target.tap();
        harness.setHand(player1, List.of(new SplinterAgingChampion()));
        addChampionMana();

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        target.untap();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Leonardo, Worldly Warrior");
        harness.assertNotInGraveyard(player2, "Leonardo, Worldly Warrior");
    }

    @Test
    void returningToHandAlsoMakesBothPlayersDraw() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterAgingChampion());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.setLibrary(player2, List.of(new LeonardoWorldlyWarrior()));

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, splinter));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Splinter, Aging Champion", "Leonardo, Worldly Warrior");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Leonardo, Worldly Warrior");
    }

    private void addChampionMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
