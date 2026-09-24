package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.f.Flamebreak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hallow.class, BarbedLightning.class, DarksteelGargoyle.class, Flamebreak.class, ArcboundWorker.class})
class HallowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from the targeted spell and gains that much life")
    void preventsTargetedSpellDamageAndGainsLife() {
        BarbedLightning barbedLightning = new BarbedLightning();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(barbedLightning));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{1}, List.of(player1.getId()));
        harness.castInstant(player1, 0, barbedLightning.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Prevents damage from the targeted spell to a creature")
    void preventsTargetedSpellDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        BarbedLightning barbedLightning = new BarbedLightning();
        harness.setHand(player1, List.of(new Hallow()));
        harness.setHand(player2, List.of(barbedLightning));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(creature.getId()));
        harness.castInstant(player1, 0, barbedLightning.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Prevents all damage from the targeted spell and gains life for every prevented damage")
    void preventsAllDamageFromTargetedSpellAndGainsLifeForEveryPreventedDamage() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        Flamebreak flamebreak = new Flamebreak();
        harness.setHand(player1, List.of(flamebreak));
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.castInstant(player2, 0, flamebreak.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(29);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
}
