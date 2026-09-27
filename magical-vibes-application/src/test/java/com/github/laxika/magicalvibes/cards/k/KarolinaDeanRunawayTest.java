package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarolinaDeanRunaway.class, GrizzlyBears.class, ThinkTwice.class})
class KarolinaDeanRunawayTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of each color to the controller's restricted pool at their first main phase")
    void addsOneManaOfEachColorAtFirstMainPhase() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();

        resolveAllTriggers();

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(color)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("The mana cannot pay for a spell cast from hand")
    void manaCannotPayForSpellFromHand() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new ThinkTwice()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana can pay for a spell cast from the graveyard")
    void manaCanPayForSpellFromGraveyard() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Think Twice");
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
