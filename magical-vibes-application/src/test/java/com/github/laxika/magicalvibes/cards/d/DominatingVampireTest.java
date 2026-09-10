package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OliviasBloodsworn;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominatingVampire.class, GrizzlyBears.class, OliviasBloodsworn.class,
        VampireOfTheDireMoon.class})
class DominatingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of, untaps, and grants haste to a creature within the Vampire count")
    void resolvesEtbEffect() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addToBattlefield(player1, new VampireOfTheDireMoon());

        castDominatingVampire(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The temporary control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new VampireOfTheDireMoon());

        castDominatingVampire(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Counts only Vampires controlled by the creature's controller")
    void doesNotCountOpponentsVampires() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new OliviasBloodsworn());
        harness.addToBattlefield(player2, new OliviasBloodsworn());

        harness.setHand(player1, List.of(new DominatingVampire()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    private void castDominatingVampire(Permanent target) {
        harness.setHand(player1, List.of(new DominatingVampire()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
