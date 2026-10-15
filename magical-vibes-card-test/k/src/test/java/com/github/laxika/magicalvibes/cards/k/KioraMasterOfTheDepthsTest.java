package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LumberingFalls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({KioraMasterOfTheDepths.class, GrizzlyBears.class, Forest.class, Shock.class, HillGiant.class, LumberingFalls.class})
class KioraMasterOfTheDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("+1 untaps up to one creature and up to one land")
    void plusOneUntapsCreatureAndLand() {
        Permanent kiora = addReadyKiora(player1, 3);
        Permanent bear = addTapped(player1, new GrizzlyBears());
        Permanent forest = addTapped(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId(), forest.getId()));
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bear.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-2 reveals four cards, takes at most one creature and land, and puts the rest into the graveyard")
    void minusTwoTakesCreatureAndLand() {
        Permanent kiora = addReadyKiora(player1, 2);
        Card bear = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(bear, forest, shock, secondShock));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        chooseLibraryCardNamed("Grizzly Bears");
        chooseLibraryCardNamed("Forest");

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(bear, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, secondShock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-8 creates an emblem and three Octopus tokens")
    void minusEightCreatesEmblemAndOctopuses() {
        Permanent kiora = addReadyKiora(player1, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        declineEmblemTriggers(findPermanents(player1, "Octopus").getFirst());

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);
        assertThat(findPermanents(player1, "Octopus")).hasSize(3);
    }

    @Test
    @DisplayName("The emblem may have an entering creature fight a target creature")
    void emblemMayHaveEnteringCreatureFight() {
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant());
        addReadyKiora(player1, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        declineEmblemTriggers(opponentGiant);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(opponentGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void plusOneCanTargetOnlyALand() {
        Permanent kiora = addReadyKiora(player1, 3);
        Permanent forest = addTapped(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOneCanTargetOnlyACreature() {
        addReadyKiora(player1, 3);
        Permanent bear = addTapped(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void plusOneCanHaveNoTargets() {
        Permanent kiora = addReadyKiora(player1, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneCanTargetTheSameAnimatedLandAsCreatureAndLand() {
        Permanent kiora = addReadyKiora(player1, 3);
        Permanent falls = harness.addToBattlefieldAndReturn(player1, new LumberingFalls());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        falls.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(falls.getId(), falls.getId()));
        harness.passBothPriorities();

        assertThat(falls.isTapped()).isFalse();
        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusTwoCanDeclineBothCardsAndLeavesFifthCardInLibrary() {
        addReadyKiora(player1, 3);
        Card bear = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        Card secondShock = new Shock();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(bear, forest, shock, secondShock, fifth));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear, forest, shock, secondShock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusTwoCanTakeOnlyALandFromAShortLibrary() {
        addReadyKiora(player1, 3);
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        chooseLibraryCardNamed("Forest");

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void declineEmblemTriggers(Permanent target) {
        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
            harness.handlePermanentChosen(player1, target.getId());
        }
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, false);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyKiora(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KioraMasterOfTheDepths());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.tap();
        return perm;
    }

    private void chooseLibraryCardNamed(String name) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf(name);
        harness.handleCardChosen(player1, index);
    }
}
