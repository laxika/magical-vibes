package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.v.VitoThornOfTheDuskRose;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastBreath.class, FreshVolunteers.class, WildJhovall.class, Forest.class})
class LastBreathTest extends BaseCardTest {

    private void giveLastBreath() {
        harness.setHand(player1, List.of(new LastBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Exiles a power-2 creature and its controller gains 4 life")
    void exilesCreatureAndControllerGainsLife() {
        Permanent target = addCreatureReady(player2, new FreshVolunteers());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Target removed from battlefield and moved to exile (not graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertNotInGraveyard(player2, "Fresh Volunteers");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Fresh Volunteers"));

        // The exiled creature's controller (player2) gains the life, not the caster
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Life goes to the controller of the exiled creature (caster's own creature)")
    void lifeGoesToCasterWhenTargetingOwnCreature() {
        Permanent target = addCreatureReady(player1, new FreshVolunteers());
        harness.setLife(player1, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        // Provide a legal target so the spell is castable at all
        addCreatureReady(player2, new FreshVolunteers());
        Permanent bigGuy = addCreatureReady(player2, new WildJhovall());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bigGuy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player2, new FreshVolunteers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        giveLastBreath();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @CardUsed(VitoThornOfTheDuskRose.class)
    @Test
    @DisplayName("Exiles a life-gain trigger source before its controller gains life")
    void exilesTargetBeforeItsLifeGainTriggerCanTrigger() {
        Permanent target = addCreatureReady(player2, new VitoThornOfTheDuskRose());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new FreshVolunteers());
        harness.setLife(player2, 20);
        giveLastBreath();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Fresh Volunteers"));
    }
}
