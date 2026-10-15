package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReturnToTheSewers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MikeyDonPartyPlanners.class, DonatelloTurtleTechie.class, Forest.class, GrizzlyBears.class,
        ReturnToTheSewers.class})
class MikeyDonPartyPlannersTest extends BaseCardTest {

    @Test
    @DisplayName("Plays a land from the top of the library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Casts a Mutant, Ninja, or Turtle creature from the top with an extra counter")
    void castsPartyCreatureWithAdditionalCounter() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        Card donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player1, List.of(donatello));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveFromLibraryTop(player1);

        Permanent permanent = findPermanent(player1, "Donatello, Turtle Techie");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast a creature without a Mutant, Ninja, or Turtle subtype from the top")
    void cannotCastNonPartyCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Does not add a counter when the party creature is cast from hand")
    void doesNotAddCounterToHandCast() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        harness.setHand(player1, List.of(new DonatelloTurtleTechie()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Donatello, Turtle Techie");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void privatelyShowsTopCardEvenWhenItCannotBeCast() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        harness.setLibrary(player1, List.of(new ReturnToTheSewers()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Return to the Sewers")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void stopsLookingAndCastingWhenSourceLeaves() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        DonatelloTurtleTechie donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player1, List.of(donatello));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(donatello);
    }

    @Test
    void counterGrantSurvivesSourceLeavingBeforeResolution() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        harness.setLibrary(player1, List.of(new DonatelloTurtleTechie()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castFromLibraryTop(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Donatello, Turtle Techie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void topLibraryCastingStillRequiresMana() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        DonatelloTurtleTechie donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player1, List.of(donatello));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(donatello);
        harness.assertNotOnBattlefield(player1, "Donatello, Turtle Techie");
    }

    @Test
    void doesNotPermitCreatureCastingDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        DonatelloTurtleTechie donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player1, List.of(donatello));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(donatello);
    }

    @Test
    void doesNotPermitSpellsWithoutPartySubtypes() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        ReturnToTheSewers spell = new ReturnToTheSewers();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1,
                harness.getPermanentId(player1, "Mikey & Don, Party Planners")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void topLibraryLandPlayDoesNotGrantAnExtraLandPlay() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        Forest secondForest = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), secondForest));

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondForest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void opponentCannotUseTheLibraryPermission() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        DonatelloTurtleTechie donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player2, List.of(donatello));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(donatello);
    }

    @Test
    void wardCountersOpponentSpellWithoutManaToPay() {
        Permanent planners = harness.addToBattlefieldAndReturn(player1, new MikeyDonPartyPlanners());
        harness.setHand(player2, List.of(new ReturnToTheSewers()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player2, 0, planners.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mikey & Don, Party Planners");
        harness.assertInGraveyard(player2, "Return to the Sewers");
        harness.assertNotOnBattlefield(player2, "Mutagen");
        assertThat(gd.stack).isEmpty();
    }
}
