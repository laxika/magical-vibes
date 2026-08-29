package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DreadDrone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BroodwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Eldrazi Spawn creatures you control get +2/+1")
    void buffsOwnEldraziSpawnCreatures() {
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();

        for (Permanent spawn : findPermanents(player1, "Eldrazi Spawn")) {
            assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Broodwarden does not buff other creatures")
    void doesNotBuffOtherCreatures() {
        harness.addToBattlefield(player1, new Broodwarden());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Broodwarden removes its bonus")
    void bonusIsRemovedWhenBroodwardenLeaves() {
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Broodwarden"));

        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
    }

    private void castDreadDrone() {
        harness.setHand(player1, List.of(new DreadDrone()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
