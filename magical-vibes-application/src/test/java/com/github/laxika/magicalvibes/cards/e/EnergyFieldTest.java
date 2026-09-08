package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage from sources controlled by an opponent")
    void preventsDamageFromOpponentSources() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from sources controlled by its controller")
    void doesNotPreventDamageFromOwnSources() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Triggers after it is put into a graveyard")
    void triggersWhenPutIntoGraveyard() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Energy Field");
    }
}
