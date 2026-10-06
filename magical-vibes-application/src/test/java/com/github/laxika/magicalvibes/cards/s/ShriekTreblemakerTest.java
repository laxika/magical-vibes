package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekTreblemaker.class, GallantCitizen.class, Shock.class})
class ShriekTreblemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card makes a target creature unable to block this turn")
    void discardMakesTargetCreatureUnableToBlock() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent blocker = addCreatureReady(player2, new GallantCitizen());
        harness.setHand(player1, List.of(new GallantCitizen()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Gallant Citizen");
    }

    @Test
    @DisplayName("Declining the first-main ability does not discard or restrict a creature")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent blocker = addCreatureReady(player2, new GallantCitizen());
        GallantCitizen cardInHand = new GallantCitizen();
        harness.setHand(player1, List.of(cardInHand));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }

    @Test
    @DisplayName("Sonic Blast deals damage when an opponent's creature dies")
    void damagesOpponentWhenTheirCreatureDies() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sonic Blast does not trigger when your own creature dies")
    void doesNotDamageWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GallantCitizen());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand cannot produce the reflexive trigger")
    void emptyHandDoesNotRestrictBlocking() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent blocker = addCreatureReady(player2, new GallantCitizen());

        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The reflexive trigger can target your own creature and resolves separately")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent creature = addCreatureReady(player1, new GallantCitizen());

        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of(new GallantCitizen()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(creature.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Gallant Citizen");
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Removing the reflexive trigger's target does not undo the discard")
    void targetCanBeRemovedInResponse() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent creature = addCreatureReady(player2, new GallantCitizen());

        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of(new GallantCitizen(), new Shock()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gallant Citizen");
        harness.assertNotOnBattlefield(player2, "Gallant Citizen");
        assertThat(creature.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first-main ability does not trigger during the opponent's turn")
    void doesNotTriggerOnOpponentsMainPhase() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        GallantCitizen cardInHand = new GallantCitizen();
        harness.setHand(player1, List.of(cardInHand));

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }

    @Test
    @DisplayName("Sonic Blast triggers for each death in the same turn")
    void damagesForEachOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new ShriekTreblemaker());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sonic Blast still deals damage after Shriek leaves the battlefield")
    void sonicBlastResolvesAfterSourceDies() {
        Permanent shriek = harness.addToBattlefieldAndReturn(player1, new ShriekTreblemaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, shriek.getId());
        harness.castAndResolveInstant(player1, 0, shriek.getId());
        harness.assertNotOnBattlefield(player1, "Shriek, Treblemaker");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
