package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.Exclude;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagesContest.class, AlabasterLeech.class, Forest.class, Exclude.class})
class MagesContestTest extends BaseCardTest {

    private void castAgainstCreatureSpell() {
        AlabasterLeech leech = new AlabasterLeech();
        harness.setHand(player1, List.of(leech));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new MagesContest()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, leech.getId());
    }

    @Test
    @DisplayName("The caster starts at one life and counters when the spell controller passes")
    void casterWinsWhenSpellControllerPasses() {
        castAgainstCreatureSpell();

        harness.handleXValueChosen(player1, 0);

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Alabaster Leech");
        harness.assertInGraveyard(player2, "Mages' Contest");
    }

    @Test
    @DisplayName("The caster can top the bid and pays only the final high bid")
    void casterTopsBid() {
        castAgainstCreatureSpell();

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 8);
        harness.handleXValueChosen(player1, 0);

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Alabaster Leech");
    }

    @Test
    @DisplayName("The targeted spell controller can win the bid and let the spell resolve")
    void spellControllerWins() {
        castAgainstCreatureSpell();

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Alabaster Leech");
        harness.assertInGraveyard(player2, "Mages' Contest");
    }

    @Test
    @DisplayName("The caster can target and counter their own spell")
    void casterCanTargetOwnSpell() {
        AlabasterLeech leech = new AlabasterLeech();
        harness.setHand(player1, List.of(leech, new MagesContest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, leech.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Alabaster Leech");
    }

    @Test
    @DisplayName("The spell fizzles without an auction if the target spell leaves the stack")
    void fizzlesIfTargetSpellLeavesStack() {
        AlabasterLeech leech = new AlabasterLeech();
        harness.setHand(player1, List.of(leech, new Exclude()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new MagesContest()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, leech.getId());
        harness.castAndResolveInstant(player1, 0, leech.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Alabaster Leech");
        harness.assertInGraveyard(player2, "Mages' Contest");
    }

    @Test
    @DisplayName("A permanent is not a legal target")
    void cannotTargetPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of(new MagesContest()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }
}
