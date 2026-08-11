package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.s.ShizoDeathsStorehouse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KethisTheHiddenHandTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary spells cost one less to cast")
    void legendarySpellsCostOneLess() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
    }

    @Test
    @DisplayName("Nonlegendary spells do not receive the cost reduction")
    void nonlegendarySpellsAreNotReduced() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation exiles only legendary cards and grants play permission to remaining legends")
    void activatesForLegendaryCards() {
        MoxAmber first = new MoxAmber();
        ShizoDeathsStorehouse second = new ShizoDeathsStorehouse();
        MoxAmber playable = new MoxAmber();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(forest, first, second, playable));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "Forest");

        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation permits a legendary land to be played from the graveyard")
    void playsLegendaryLandFromGraveyard() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(
                new MoxAmber(), new ShizoDeathsStorehouse(), new ShizoDeathsStorehouse()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Shizo, Death's Storehouse");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard play permission expires at end of turn")
    void graveyardPlayPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(new MoxAmber(), new ShizoDeathsStorehouse(), new MoxAmber()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
