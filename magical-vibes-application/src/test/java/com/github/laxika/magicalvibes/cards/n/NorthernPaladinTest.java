package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthernPaladin.class, ScatheZombies.class, GrizzlyBears.class, BadMoon.class})
class NorthernPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target black permanent")
    void resolvingDestroysTargetBlackPermanent() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Northern Paladin").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Resolving destroys a black noncreature permanent")
    void resolvingDestroysBlackNoncreaturePermanent() {
        setupPaladin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BadMoon());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bad Moon");
        harness.assertInGraveyard(player2, "Bad Moon");
    }

    @Test
    @DisplayName("Cannot target a non-black permanent")
    void cannotTargetNonBlackPermanent() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black permanent");
    }

    private void setupPaladin() {
        addCreatureReady(player1, new NorthernPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
