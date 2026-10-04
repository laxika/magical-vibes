package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameWave.class, GrizzlyBears.class, ChandraNalaar.class, Boomerang.class, PaladinEnVec.class})
class FlameWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player and each creature that player controls")
    void deals4DamageToPlayerAndTheirCreatures() {
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Target player takes 4 damage
        harness.assertLife(player2, 16);
        // Both 2/2 bears die to 4 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 4 damage to target planeswalker and each creature its controller controls")
    void deals4DamageToPlaneswalkerAndTheirCreatures() {
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Caster's creature is unharmed
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target its controller and damages that player's creatures")
    void damagesItsControllerAndTheirCreatures() {
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from red prevents creature damage without preventing damage to other recipients")
    void protectionPreventsOnlyProtectedCreaturesDamage() {
        var protectedCreature = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "Paladin en-Vec");
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An absent planeswalker target prevents all damage, including damage to its controller's creatures")
    void absentPlaneswalkerTargetPreventsAllDamage() {
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameWave()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Chandra Nalaar");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Flame Wave");
    }
}
