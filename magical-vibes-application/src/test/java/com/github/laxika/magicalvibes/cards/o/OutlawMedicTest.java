package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutlawMedic.class, DoomBlade.class})
class OutlawMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent medic = addCreatureReady(player1, new OutlawMedic());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(medic)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When Outlaw Medic dies, its controller draws a card")
    void deathTriggerDrawsCard() {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new OutlawMedic());

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Outlaw Medic"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Outlaw Medic");
    }
}
