package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SageOfAncientLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Front face power and toughness equal the controller's hand size")
    void frontFaceUsesControllerHandSize() {
        Permanent sage = addSageReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersAndDrawsCard() {
        harness.setHand(player1, List.of(new SageOfAncientLore(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Front face transforms when no spells were cast last turn")
    void transformsToWerewolfWhenNoSpellsWereCast() {
        Permanent sage = addSageReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.spellsCastLastTurn.clear();

        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(sage.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Back face power and toughness equal all players' total hand size")
    void backFaceUsesTotalHandSize() {
        Permanent sage = addSageReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face transforms when a player cast two spells last turn")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent sage = addSageReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(sage.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(sage.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(gd.playerHands.get(player1.getId()).size());
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(gd.playerHands.get(player1.getId()).size());
    }

    private Permanent addSageReady(Player player) {
        Permanent permanent = new Permanent(new SageOfAncientLore());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
