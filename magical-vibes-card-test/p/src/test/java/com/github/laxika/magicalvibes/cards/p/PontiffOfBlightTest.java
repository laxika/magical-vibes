package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaneAlleyBlackguard;
import com.github.laxika.magicalvibes.cards.t.TitheDrinker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PontiffOfBlight.class, BaneAlleyBlackguard.class, TitheDrinker.class})
class PontiffOfBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Pontiff's own extort drains for 1 when paid")
    void ownExtortDrains() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Another creature you control gains extort, so each instance triggers separately")
    void grantsExtortToOtherCreatures() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player1, new BaneAlleyBlackguard());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Opponent's creatures do not gain extort")
    void opponentCreaturesDoNotGainExtort() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player2, new BaneAlleyBlackguard());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Extort may be paid with white mana")
    void paysWithWhiteMana() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining extort leaves both life totals unchanged")
    void declinesExtort() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with printed extort also gets a separate granted instance")
    void printedAndGrantedExtortTriggerSeparately() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19 - i);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21 + i);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger your extort abilities")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player1, new BaneAlleyBlackguard());
        harness.setHand(player2, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Pontiffs each have their own extort and the other's granted extort")
    void multiplePontiffsGrantIndependentInstances() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19 - i);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21 + i);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining one extort instance does not prevent paying another")
    void declinesOneInstanceAndPaysAnother() {
        harness.addToBattlefield(player1, new PontiffOfBlight());
        harness.addToBattlefield(player1, new BaneAlleyBlackguard());
        harness.setHand(player1, List.of(new BaneAlleyBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }
}
