package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForbiddenFriendship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MythosOfIlluna;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathsOasis.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        ForbiddenFriendship.class, MythosOfIlluna.class, Opalescence.class})
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

    @Test
    void cannotDeclineReturningAnEligibleCreature() {
        Card returned = new GrizzlyBears();
        harness.setLibrary(player1, List.of(returned, new Forest()));
        harness.addToBattlefield(player1, new DeathsOasis());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot decline forced graveyard choice");

        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(returned));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void returnsExistingGraveyardCreatureEvenWithEmptyLibrary() {
        Card returned = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(returned));
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

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void ignoresCreatureTokenDeath() {
        harness.addToBattlefield(player1, new DeathsOasis());
        harness.setHand(player1, List.of(new ForbiddenFriendship()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent token = findPermanents(player1, "Dinosaur").getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    void gainsNoLifeWithoutControlledCreatures() {
        harness.addToBattlefield(player1, new DeathsOasis());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Death's Oasis");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void determinesGreatestManaValueAtResolution() {
        harness.addToBattlefield(player1, new DeathsOasis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent greatest = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, greatest));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Death's Oasis");
    }

    @Test
    void animatedNontokenOasisTriggersForItsOwnDeath() {
        Card returned = new GrizzlyBears();
        harness.setLibrary(player1, List.of(returned, new Forest()));
        Permanent oasis = harness.addToBattlefieldAndReturn(player1, new DeathsOasis());
        harness.addToBattlefield(player1, new Opalescence());
        assertThat(gqs.isCreature(gd, oasis)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, oasis));
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int returnedIndex = gd.playerGraveyards.get(player1.getId()).indexOf(returned);
        assertThat(choice.validIndices()).containsExactly(returnedIndex);
        harness.handleGraveyardCardChosen(player1, returnedIndex);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Death's Oasis");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void animatedTokenCopyDoesNotTriggerForItsOwnDeath() {
        Permanent oasis = harness.addToBattlefieldAndReturn(player1, new DeathsOasis());
        harness.addToBattlefield(player1, new Opalescence());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0, oasis.getId());
        List<Permanent> copies = findPermanents(player1, "Death's Oasis");
        assertThat(copies).hasSize(2);
        Permanent token = copies.stream().filter(p -> !p.getId().equals(oasis.getId())).findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }
}
