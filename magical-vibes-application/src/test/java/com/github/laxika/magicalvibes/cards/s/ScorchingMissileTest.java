package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScorchingMissile.class, ElspethKnightErrant.class, AvenFlock.class})
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

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 4);
    }

    @Test
    @DisplayName("Deals 4 damage when its controller is the targeted player")
    void damageToItsController() {
        giveScorchingMissile();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, lifeBefore - 4);
    }

    @Test
    @DisplayName("Deals 4 damage to a targeted planeswalker")
    void damageToTargetPlaneswalker() {
        Permanent elspeth = new Permanent(new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        giveScorchingMissile();

        harness.castAndResolveSorcery(player1, 0, elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AvenFlock());
        giveScorchingMissile();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Aven Flock")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback deals 4 damage and exiles Scorching Missile")
    void flashbackDealsDamageAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 4);
        harness.assertNotInGraveyard(player1, "Scorching Missile");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Scorching Missile"));
    }

    @Test
    @DisplayName("Flashback requires nine generic mana")
    void flashbackRequiresNineGenericMana() {
        harness.setGraveyard(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires one red mana")
    void flashbackRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new ScorchingMissile()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
