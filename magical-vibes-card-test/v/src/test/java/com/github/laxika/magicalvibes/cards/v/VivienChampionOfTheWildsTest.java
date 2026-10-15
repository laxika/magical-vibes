package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienChampionOfTheWilds.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class VivienChampionOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Lets its controller cast creature spells during an opponent's turn")
    void grantsFlashToCreatureSpells() {
        addReadyVivien(3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("+1 grants reach and vigilance until your next turn")
    void plusOneGrantsKeywordsUntilNextTurn() {
        Permanent vivien = addReadyVivien(3);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int vivienIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vivien);

        harness.activateAbility(player1, vivienIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("+1 may choose no target")
    void plusOneMayChooseNoTarget() {
        Permanent vivien = addReadyVivien(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 exiles one card face down, bottoms the rest, and permits casting a creature")
    void minusTwoExilesCreatureWithCastPermission() {
        Permanent vivien = addReadyVivien(3);
        Card first = new Shock();
        Card chosen = new GrizzlyBears();
        Card third = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, chosen, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(first, chosen, third);

        harness.handleCardChosen(player1, search.params().cards().indexOf(chosen));

        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(
                PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(first, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(exiled.sourcePermanentId()).isEqualTo(vivien.getId());
        assertThat(gd.getCardsExiledByPermanent(vivien.getId())).containsExactly(chosen);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, third);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 does not permit casting a selected noncreature card")
    void minusTwoDoesNotPermitNoncreature() {
        addReadyVivien(3);
        Card chosen = new Shock();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), chosen, new LlanowarElves()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(chosen));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void exiledCreatureRemainsCastableAfterVivienLeaves() {
        Permanent vivien = addReadyVivien(3);
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, vivien);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void minusTwoStillGrantsPermissionWhenLoyaltyCostKillsVivien() {
        addReadyVivien(2);
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vivien, Champion of the Wilds");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void minusTwoOnEmptyLibraryOnlyPaysLoyalty() {
        Permanent vivien = addReadyVivien(3);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(vivien.getId())).isEmpty();
    }

    @Test
    void minusTwoWithTwoCardsExilesChosenCardAndBottomsTheOther() {
        Permanent vivien = addReadyVivien(3);
        Card remaining = new Shock();
        Card chosen = new LlanowarElves();
        harness.setLibrary(player1, List.of(remaining, chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getCardsExiledByPermanent(vivien.getId())).containsExactly(chosen);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
    }

    @Test
    void singleNoncreatureIsExiledWithoutCastingPermission() {
        Permanent vivien = addReadyVivien(3);
        Card chosen = new Shock();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(vivien.getId())).containsExactly(chosen);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void exiledCreatureCanBeCastDuringOpponentsTurnWhileVivienRemains() {
        addReadyVivien(3);
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void minusTwoPreservesChosenBottomOrderAndUntouchedLibrary() {
        addReadyVivien(3);
        Card first = new Shock();
        Card chosen = new GrizzlyBears();
        Card third = new LlanowarElves();
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third, untouched));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
    }

    @Test
    void plusOneExpiresAtControllersNextTurnAndSurvivesSourceRemoval() {
        Permanent vivien = addReadyVivien(3);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, vivien);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void plusOneCanTargetOpponentsCreature() {
        addReadyVivien(3);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void flashPermissionDoesNotExtendToOpponent() {
        addReadyVivien(3);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVivien(int loyalty) {
        Permanent vivien = harness.addToBattlefieldAndReturn(player1, new VivienChampionOfTheWilds());
        vivien.setCounterCount(CounterType.LOYALTY, loyalty);
        vivien.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return vivien;
    }
}
