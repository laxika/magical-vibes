package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalevolentAwakening.class, DuskImp.class, Gravedigger.class, CoffinPurge.class})
class MalevolentAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature returns a target creature card from the graveyard to hand")
    void sacrificesCreatureAndReturnsTargetCreatureToHand() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new DuskImp());
        Card target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.assertOnBattlefield(player1, "Malevolent Awakening");
        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Gravedigger");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Gravedigger");
        harness.assertInHand(player1, "Gravedigger");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new DuskImp());
        Card target = new CoffinPurge();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Coffin Purge");
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureCardInOpponentsGraveyard() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new DuskImp());
        Card target = new Gravedigger();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dusk Imp");
        harness.assertInGraveyard(player2, "Gravedigger");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        Card target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Malevolent Awakening");
        harness.assertInGraveyard(player1, "Gravedigger");
    }

    @Test
    @DisplayName("Fizzling does not return the target when it leaves the graveyard")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new DuskImp());
        Card target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Gravedigger");
        harness.assertInGraveyard(player1, "Dusk Imp");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
