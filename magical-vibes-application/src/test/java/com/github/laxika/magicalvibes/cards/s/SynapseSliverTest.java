package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynapseSliver.class, MetallicSliver.class, GrizzlyBears.class, Forest.class})
class SynapseSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver's combat damage lets its controller draw a card")
    void controllerMayDrawForSliverCombatDamage() {
        addAttackingCreature(player1, new SynapseSliver());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the combat-damage choice does not draw")
    void controllerMayDeclineTheDraw() {
        addAttackingCreature(player1, new SynapseSliver());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The ability also lets an opposing Sliver's controller draw")
    void opposingSliverControllerMayDraw() {
        addCreatureReady(player1, new SynapseSliver());
        addAttackingCreature(player2, new MetallicSliver());
        harness.setLibrary(player2, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A non-Sliver does not gain the draw ability")
    void nonSliverDoesNotTrigger() {
        addAttackingCreature(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

}
