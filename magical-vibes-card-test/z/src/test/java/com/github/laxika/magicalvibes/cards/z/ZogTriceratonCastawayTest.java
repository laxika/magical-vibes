package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZogTriceratonCastaway.class, Mountain.class, Forest.class})
class ZogTriceratonCastawayTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZogTriceratonCastaway());
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Mountaincycling discards Zog and offers only Mountain cards")
    void mountaincyclingDiscardsAndOffersMountains() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Mountain()));

        harness.activateHandAbility(player1, 0, null);
        harness.assertNotInHand(player1, "Zog, Triceraton Castaway");
        harness.assertInGraveyard(player1, "Zog, Triceraton Castaway");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zog, Triceraton Castaway");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Mountain)
                .hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Zog can target itself with its enters ability")
    void etbCanTargetItself() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent zog = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, zog.getId());
        harness.passBothPriorities();

        assertThat(zog.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Mountaincycling may fail to find even when a Mountain is available")
    void mountaincyclingMayFailToFind() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Zog, Triceraton Castaway");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mountaincycling resolves without drawing when no Mountain exists")
    void mountaincyclingWithNoMountains() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zog, Triceraton Castaway");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mountaincycling requires two mana before discarding")
    void mountaincyclingCannotBeActivatedWithInsufficientMana() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Zog, Triceraton Castaway");
        harness.assertNotInGraveyard(player1, "Zog, Triceraton Castaway");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocking restriction expires when the turn ends")
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZogTriceratonCastaway());
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
