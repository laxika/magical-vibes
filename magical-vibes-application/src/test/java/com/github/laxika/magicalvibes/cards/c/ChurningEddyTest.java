package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChurningEddy.class, AvenTrooper.class, TaintedIsle.class})
class ChurningEddyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and target land to their owners' hands")
    void returnsTargetCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Trooper");
        harness.assertNotOnBattlefield(player2, "Tainted Isle");
        harness.assertInHand(player2, "Aven Trooper");
        harness.assertInHand(player2, "Tainted Isle");
    }

    @Test
    @DisplayName("Still returns the land if the creature target becomes illegal")
    void returnsLandIfCreatureTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tainted Isle");
        harness.assertInHand(player2, "Tainted Isle");
    }

    @Test
    @DisplayName("Rejects a non-creature as the creature target")
    void rejectsNonCreatureCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(TreetopVillage.class)
    @DisplayName("Allows a land creature to be chosen as both targets")
    void allowsLandCreatureAsBothTargets() {
        Permanent village = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(village.getId(), village.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Treetop Village");
        harness.assertInHand(player2, "Treetop Village");
    }
}
