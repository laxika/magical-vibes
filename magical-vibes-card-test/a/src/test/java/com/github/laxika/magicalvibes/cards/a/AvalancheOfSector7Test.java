package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CompositeGolem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvalancheOfSector7.class, CompositeGolem.class, Ornithopter.class,
        RodOfRuin.class, ShivanDragon.class})
class AvalancheOfSector7Test extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts opponents control and toughness is three")
    void powerCountsOpponentsArtifacts() {
        Permanent avalanche = harness.addToBattlefieldAndReturn(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, avalanche)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avalanche)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent activating an artifact ability deals damage to that opponent")
    void opponentArtifactActivationDealsDamage() {
        harness.addToBattlefield(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player2, new CompositeGolem());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Power updates when an opponent sacrifices their last artifact")
    void powerUpdatesWhenArtifactLeaves() {
        Permanent avalanche = harness.addToBattlefieldAndReturn(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, avalanche)).isZero();

        harness.addToBattlefield(player2, new CompositeGolem());
        assertThat(gqs.getEffectivePower(gd, avalanche)).isEqualTo(1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, avalanche)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, avalanche)).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller's artifact activations do not trigger damage")
    void ownArtifactActivationDoesNotDealDamage() {
        harness.addToBattlefield(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player1, new CompositeGolem());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent's nonartifact activations do not trigger damage")
    void nonartifactActivationDoesNotDealDamage() {
        harness.addToBattlefield(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player2, new ShivanDragon());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Nonmana artifact activation deals damage before the activated ability resolves")
    void nonmanaArtifactActivationTriggersBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
