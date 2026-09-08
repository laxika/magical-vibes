package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlamebreakTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each player and each creature without flying")
    void damagesPlayersAndNonflyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFlamebreak();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creatures dealt damage by Flamebreak can't be regenerated this turn")
    void damagedCreaturesCannotRegenerate() {
        DrudgeSkeletons skeletons = new DrudgeSkeletons();
        Permanent skeletonPermanent = new Permanent(skeletons);
        skeletonPermanent.setSummoningSick(false);
        skeletonPermanent.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(skeletonPermanent);

        castFlamebreak();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    private void castFlamebreak() {
        harness.setHand(player1, List.of(new Flamebreak()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
