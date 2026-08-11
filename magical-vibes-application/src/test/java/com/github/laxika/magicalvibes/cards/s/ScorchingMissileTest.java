package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScorchingMissileTest extends BaseCardTest {

    private void giveScorchingMissile() {
        harness.setHand(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals 4 damage to the targeted player")
    void damageToTargetPlayer() {
        giveScorchingMissile();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Deals 4 damage to a targeted planeswalker")
    void damageToTargetPlaneswalker() {
        Permanent elspeth = new Permanent(new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        giveScorchingMissile();

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveScorchingMissile();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback deals 4 damage and exiles Scorching Missile")
    void flashbackDealsDamageAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        harness.assertNotInGraveyard(player1, "Scorching Missile");
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Scorching Missile"));
    }
}
