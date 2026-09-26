package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.h.HoverguardObserver;
import com.github.laxika.magicalvibes.cards.t.TangleSpider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flamebreak.class, TangleSpider.class, HoverguardObserver.class, AuriokGlaivemaster.class})
class FlamebreakTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each player and each creature without flying")
    void damagesPlayersAndNonflyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new TangleSpider());
        Permanent ownFlyingCreature = harness.addToBattlefieldAndReturn(player1, new HoverguardObserver());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new TangleSpider());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new HoverguardObserver());

        castFlamebreak();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(ownFlyingCreature.getMarkedDamage()).isZero();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creatures dealt damage by Flamebreak can't be regenerated this turn")
    void damagedCreaturesCannotRegenerate() {
        Permanent creature = addCreatureReady(player2, new AuriokGlaivemaster());
        creature.setRegenerationShield(1);

        castFlamebreak();

        harness.assertNotOnBattlefield(player2, "Auriok Glaivemaster");
        harness.assertInGraveyard(player2, "Auriok Glaivemaster");
    }

    private void castFlamebreak() {
        harness.castFromHand(player1, new Flamebreak(), "{R}{R}{R}");
        harness.passBothPriorities();
    }
}
