package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FodderCannon.class, GrizzlyBears.class, AirElemental.class})
class FodderCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature, killing it; taps and sacrifices as cost")
    void deals4DamageToTargetCreature() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears()); // sacrifice fodder
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2 victim
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        assertThat(cannon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature survives if toughness is high enough")
    void creatureSurvivesWithHighToughness() {
        harness.addToBattlefield(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4
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
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // 1 short

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Fodder Cannon is tapped")
    void cannotActivateWhileTapped() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new FodderCannon());
        Permanent firstFodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, victim.getId());
        Permanent secondFodder = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();

        assertThat(cannon.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cannon, secondFodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstFodder.getCard());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new FodderCannon());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");

        assertThat(cannon.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FodderCannon());
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }
}
