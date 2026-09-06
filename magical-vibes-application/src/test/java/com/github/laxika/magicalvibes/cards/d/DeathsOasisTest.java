package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathsOasis.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class DeathsOasisTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two, then returns a creature card with lesser mana value")
    void millsThenReturnsLesserCreature() {
        Card returned = new GrizzlyBears();
        Card noncreature = new Forest();
        harness.setLibrary(player1, List.of(returned, noncreature));
        harness.addToBattlefield(player1, new DeathsOasis());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int returnedIndex = gd.playerGraveyards.get(player1.getId()).indexOf(returned);
        assertThat(choice.validIndices()).containsExactly(returnedIndex);

        harness.handleGraveyardCardChosen(player1, returnedIndex);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not return a creature card with equal or greater mana value")
    void requiresStrictlyLowerManaValue() {
        Card equal = new GrizzlyBears();
        Card greater = new AirElemental();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(equal, greater));
        harness.addToBattlefield(player1, new DeathsOasis());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void ignoresOpponentsCreatureDeath() {
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, new DeathsOasis());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Gains life equal to the greatest mana value among controlled creatures")
    void gainsLifeFromGreatestControlledCreatureManaValue() {
        harness.addToBattlefield(player1, new DeathsOasis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertInGraveyard(player1, "Death's Oasis");
    }
}
