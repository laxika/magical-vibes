package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetalworkColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts reduce the generic casting cost by their total mana value")
    void noncreatureArtifactsReduceCastingCostByTotalManaValue() {
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new MyrEnforcer());
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new MetalworkColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrificing two artifacts returns Metalwork Colossus from the graveyard to hand")
    void sacrificingTwoArtifactsReturnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new MetalworkColossus()));
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new DarksteelIngot());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Metalwork Colossus");
        harness.assertNotInGraveyard(player1, "Metalwork Colossus");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
