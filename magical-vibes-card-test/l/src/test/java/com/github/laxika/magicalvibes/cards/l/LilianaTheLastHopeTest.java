package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianaTheLastHope.class, Forest.class, Gravecrawler.class, GrizzlyBears.class, HillGiant.class})
class LilianaTheLastHopeTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives up to one creature -2/-1 until Liliana's controller's next turn")
    void plusOneShrinksCreatureUntilNextTurn() {
        addReadyLiliana(player1, 4);
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 may choose no creature")
    void plusOneMayChooseNoCreature() {
        addReadyLiliana(player1, 4);
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("-2 mills two cards then may return a creature card from the graveyard")
    void minusTwoMillsThenReturnsCreature() {
        Permanent liliana = addReadyLiliana(player1, 4);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int creatureIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, creatureIndex);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("-2 can be declined after milling")
    void minusTwoMayBeDeclined() {
        addReadyLiliana(player1, 4);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("-7 creates two plus controlled Zombies at the controller's end step")
    void minusSevenCreatesDynamicZombieEmblem() {
        Permanent liliana = addReadyLiliana(player1, 7);
        addCreatureReady(player1, new Gravecrawler());
        addCreatureReady(player1, new Gravecrawler());
        addCreatureReady(player2, new Gravecrawler());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isZero();
        advanceIntoEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).hasSize(4);
    }

    @Test
    @DisplayName("+1 cannot target a noncreature permanent")
    void plusOneCannotTargetLand() {
        addReadyLiliana(player1, 4);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 keeps its effect through the opponent's turn after Liliana leaves")
    void plusOnePersistsWithoutLiliana() {
        Permanent liliana = addReadyLiliana(player1, 4);
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(liliana);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 puts a creature with zero toughness into its owner's graveyard")
    void plusOneKillsOneToughnessCreature() {
        addReadyLiliana(player1, 4);
        addCreatureReady(player2, new Gravecrawler());
        Permanent target = findPermanent(player2, "Gravecrawler");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gravecrawler");
        harness.assertInGraveyard(player2, "Gravecrawler");
    }

    @Test
    @DisplayName("-2 can return an existing creature even with fewer than two cards to mill")
    void minusTwoReturnsExistingCreatureFromShortLibrary() {
        addReadyLiliana(player1, 4);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The emblem creates two Zombies from zero and does not trigger on an opponent's end step")
    void emblemStartsFromZeroAndOnlyTriggersOnOwnEndStep() {
        addReadyLiliana(player1, 7);
        addCreatureReady(player2, new Gravecrawler());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Liliana, the Last Hope");

        advanceIntoEndStep(player2);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        advanceIntoEndStep(player1);
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("The emblem counts Zombies at resolution rather than when it triggers")
    void emblemCountsZombiesAtResolution() {
        addReadyLiliana(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        addCreatureReady(player1, new Gravecrawler());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(3);
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaTheLastHope());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
