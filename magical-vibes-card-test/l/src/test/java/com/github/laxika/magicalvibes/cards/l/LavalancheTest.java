package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lavalanche.class, GrizzlyBears.class, GiantSpider.class, JaceBeleren.class})
class LavalancheTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player and each creature that player controls")
    void dealsXDamageToPlayerAndTheirCreatures() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, dies to 3
        harness.addToBattlefield(player2, new GiantSpider());   // 2/4, survives 3

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Target player takes X (=3) damage
        harness.assertLife(player2, 17);
        // The 2/2 dies to 3 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // The 2/4 survives 3 damage
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Caster's creature is unharmed
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void damagesPlaneswalkerAndItsControllersCreaturesWithoutDamagingController() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        var jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard() instanceof GrizzlyBears).findFirst().orElseThrow()
                .getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
    }

    @Test
    void canTargetCasterAndDamageOnlyTheirCreatures() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isZero();
    }

    @Test
    void zeroXDealsNoDamage() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void missingPlaneswalkerTargetPreventsAllDamage() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        var jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, jace.getId());
        gd.playerBattlefields.get(player2.getId()).remove(jace);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
