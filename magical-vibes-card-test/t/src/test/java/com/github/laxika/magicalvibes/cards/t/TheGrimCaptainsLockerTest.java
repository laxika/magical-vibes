package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGrimCaptainsLocker.class, GrizzlyBears.class, Shock.class})
class TheGrimCaptainsLockerTest extends BaseCardTest {

    @Test
    void surveilsOne() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void grantsCreatureCardsEscapeUntilEndOfTurn() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
    }

    @Test
    void doesNotGrantEscapeToNonCreatureCards() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setGraveyard(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapeGrantExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void surveilCanLeaveTheCardOnTop() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void surveilWithAnEmptyLibraryNeedsNoChoice() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void bothAbilitiesRequireTappingTheLocker() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void requiresFourOtherCardsForEscape() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeDoesNotAllowCastingCreaturesAtInstantSpeed() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotGrantEscapeToCreaturesThatDieAfterResolution() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        int creatureIndex = gd.playerGraveyards.get(player1.getId()).indexOf(creature.getOriginalCard());
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, creatureIndex, List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsEscapeToCreaturesThatDieBeforeResolution() {
        harness.addToBattlefield(player1, new TheGrimCaptainsLocker());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        int creatureIndex = gd.playerGraveyards.get(player1.getId()).indexOf(creature.getOriginalCard());
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);
        harness.castFromGraveyard(player1, creatureIndex, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
    }
}
