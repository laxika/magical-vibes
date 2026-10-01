package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.c.Cryoclasm;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarienKingOfKjeldor.class, Cryoclasm.class, SnowCoveredIsland.class, BorealCentaur.class})
class DarienKingOfKjeldorTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to you lets you create that many Soldier tokens when accepted")
    void damageCreatesThatManySoldierTokensWhenAccepted() {
        harness.addToBattlefield(player1, new DarienKingOfKjeldor());
        harness.setLife(player1, 20);
        Permanent targetLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, targetLand.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(3);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Declining the damage trigger creates no Soldier tokens")
    void decliningCreatesNoSoldierTokens() {
        harness.addToBattlefield(player1, new DarienKingOfKjeldor());
        harness.setLife(player1, 20);
        Permanent targetLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, targetLand.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to you creates that much Soldier tokens when accepted")
    void combatDamageCreatesThatManySoldierTokens() {
        harness.addToBattlefield(player1, new DarienKingOfKjeldor());
        Permanent attacker = addCreatureReady(player2, new BorealCentaur());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }
}
