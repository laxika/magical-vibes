package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CabalPatriarch;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfGrace.class, ScreamsOfTheDamned.class, FlameBurst.class, DuskImp.class,
        CabalPatriarch.class})
class SphereOfGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents black noncombat damage to the controller")
    void preventsBlackNoncombatDamage() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.addToBattlefield(player2, new ScreamsOfTheDamned());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new DuskImp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage from a nonblack source")
    void doesNotPreventNonblackDamage() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents black combat damage to the controller")
    void preventsBlackCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new DuskImp());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents 2 damage from each black combat source")
    void preventsDamageFromEachBlackCombatSource() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new DuskImp());
        addCreatureReady(player2, new DuskImp());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents only 2 damage from a larger black source")
    void reducesLargerBlackCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new CabalPatriarch());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Multiple Spheres each prevent 2 damage from the same source")
    void multipleSpheresReduceTheSameDamageEvent() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new CabalPatriarch());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevention applies again to each damage event from the same source")
    void preventsRepeatedDamageFromSameSource() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.addToBattlefield(player2, new ScreamsOfTheDamned());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new DuskImp(), new DuskImp()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not prevent black damage to the controller's creatures")
    void doesNotProtectCreatures() {
        harness.addToBattlefield(player1, new SphereOfGrace());
        harness.addToBattlefield(player1, new DuskImp());
        harness.addToBattlefield(player2, new ScreamsOfTheDamned());
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new DuskImp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Dusk Imp");
    }
}
