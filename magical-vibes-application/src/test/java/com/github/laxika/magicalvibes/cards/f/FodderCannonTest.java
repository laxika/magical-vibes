package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FodderCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature, killing it; taps and sacrifices as cost")
    void deals4DamageToTargetCreature() {
        harness.addToBattlefield(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears()); // sacrifice fodder
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, victim);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fodder Cannon").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature survives if toughness is high enough")
    void creatureSurvivesWithHighToughness() {
        harness.addToBattlefield(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental()); // 4/4
        Permanent victim = findPermanent(player2, "Air Elemental");
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 5/5, survives 4 damage
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // 1 short

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim))
                .isInstanceOf(IllegalStateException.class);
    }
}
