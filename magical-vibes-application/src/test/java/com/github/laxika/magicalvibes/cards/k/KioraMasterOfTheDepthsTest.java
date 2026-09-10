package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KioraMasterOfTheDepths.class, GrizzlyBears.class, Forest.class, Shock.class, HillGiant.class})
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
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
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
        Permanent perm = new Permanent(new KioraMasterOfTheDepths());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void chooseLibraryCardNamed(String name) {
        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf(name);
        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
