package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrystallineNautilusTest extends BaseCardTest {

    @Test
    @DisplayName("Crystalline Nautilus can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new CrystallineNautilus()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent nautilus = findPermanent(player1, "Crystalline Nautilus");
        assertThat(gqs.isCreature(gd, nautilus)).isTrue();
    }

    @Test
    @DisplayName("Crystalline Nautilus bestowed to a creature grants +4/+4")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrystallineNautilus()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent nautilus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.isCreature(gd, nautilus)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
    }

    @Test
    @DisplayName("Crystalline Nautilus sacrifices itself when targeted as a creature")
    void sacrificesWhenTargetedAsCreature() {
        harness.addToBattlefield(player1, new CrystallineNautilus());
        Permanent nautilus = findPermanent(player1, "Crystalline Nautilus");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, nautilus.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystalline Nautilus");
        harness.assertInGraveyard(player1, "Crystalline Nautilus");
    }

    @Test
    @DisplayName("A bestowed Crystalline Nautilus sacrifices the enchanted creature when it is targeted")
    void sacrificesEnchantedCreatureWhenTargeted() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrystallineNautilus()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Crystalline Nautilus");
    }
}
