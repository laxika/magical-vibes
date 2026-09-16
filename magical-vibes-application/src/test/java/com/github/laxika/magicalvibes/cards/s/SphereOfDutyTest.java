package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrashingCentaur;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.h.HowlingGale;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfDuty.class, HowlingGale.class, FlameBurst.class, CrashingCentaur.class})
class SphereOfDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents green noncombat damage to the controller")
    void preventsDamageFromGreenSource() {
        harness.addToBattlefield(player1, new SphereOfDuty());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new HowlingGale()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage from a non-green source")
    void doesNotPreventDamageFromNonGreenSource() {
        harness.addToBattlefield(player1, new SphereOfDuty());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Protects only its controller from green damage")
    void protectsOnlyItsController() {
        harness.addToBattlefield(player1, new SphereOfDuty());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HowlingGale()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents green combat damage to the controller")
    void preventsGreenCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfDuty());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new CrashingCentaur());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents 2 damage from each green combat source")
    void preventsDamageFromEachGreenCombatSource() {
        harness.addToBattlefield(player1, new SphereOfDuty());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new CrashingCentaur());
        addCreatureReady(player2, new CrashingCentaur());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
