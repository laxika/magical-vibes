package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThelonsCurse.class, RiverMerfolk.class, IcatianPhalanx.class})
class ThelonsCurseTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped blue creature stays tapped through the untap step while a non-blue one untaps")
    void blueCreatureStaysTappedWhileWhiteUntaps() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent merfolk = addTapped(player1, new RiverMerfolk());
        Permanent soldier = addTapped(player1, new IcatianPhalanx());

        advanceToNextTurn(player2);

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying blue mana untaps the chosen blue creature")
    void payingBlueManaUntapsChosenBlueCreature() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent merfolk = addTapped(player1, new RiverMerfolk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(merfolk.getId()));

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the blue untap cost")
    void colorlessManaCannotPayBlueUntapCost() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent merfolk = addTapped(player1, new RiverMerfolk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the blue creature tapped")
    void choosingNoneLeavesBlueCreatureTapped() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent merfolk = addTapped(player1, new RiverMerfolk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(merfolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each player's upkeep trigger uses that player's blue mana and creatures")
    void eachPlayerMayPayForTheirOwnBlueCreature() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent merfolk = addTapped(player2, new RiverMerfolk());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(merfolk.getId()));

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each chosen blue creature costs one blue mana")
    void payingForMultipleBlueCreaturesUntapsEachChosenCreature() {
        harness.addToBattlefield(player1, new ThelonsCurse());
        Permanent firstMerfolk = addTapped(player1, new RiverMerfolk());
        Permanent secondMerfolk = addTapped(player1, new RiverMerfolk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstMerfolk.getId(), secondMerfolk.getId()));

        assertThat(firstMerfolk.isTapped()).isFalse();
        assertThat(secondMerfolk.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UPKEEP);
    }
}
