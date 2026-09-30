package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BrawlersPlate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SteelclawLance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutfittedJouster.class, SteelclawLance.class, BrawlersPlate.class, Shock.class})
class OutfittedJousterTest extends BaseCardTest {

    @Test
    void conjuresAndAttachesBothEquipment() {
        Permanent jouster = castJouster();

        List<Permanent> equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Steelclaw Lance")
                        || permanent.getCard().getName().equals("Brawler's Plate"))
                .toList();

        assertThat(equipment).hasSize(2);
        assertThat(equipment).allMatch(permanent -> jouster.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void preventsDamageAndSacrificesOneAttachedEquipmentPerEvent() {
        Permanent jouster = castJouster();

        castShock(jouster);
        assertThat(attachedEquipmentCount(jouster)).isEqualTo(1);
        assertThat(jouster.getMarkedDamage()).isZero();

        castShock(jouster);
        assertThat(attachedEquipmentCount(jouster)).isZero();
        assertThat(jouster.getMarkedDamage()).isZero();

        castShock(jouster);
        harness.assertInGraveyard(player1, "Outfitted Jouster");
    }

    private Permanent castJouster() {
        harness.setHand(player1, List.of(new OutfittedJouster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Outfitted Jouster");
    }

    private void castShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private long attachedEquipmentCount(Permanent jouster) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> jouster.getId().equals(permanent.getAttachedTo()))
                .count();
    }
}
