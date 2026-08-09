package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrossbowAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crossbow Ambush grants reach to creatures you control")
    void grantsReachToOwnCreatures() {
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        castCrossbowAmbush();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.REACH);
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.REACH);
    }

    @Test
    @DisplayName("Reach granted by Crossbow Ambush expires at end of turn")
    void reachExpiresAtEndOfTurn() {
        Permanent ownCreature = addCreature(player1);

        castCrossbowAmbush();
        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.REACH);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords()).doesNotContain(Keyword.REACH);
    }

    private void castCrossbowAmbush() {
        harness.setHand(player1, List.of(new CrossbowAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
