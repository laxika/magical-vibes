package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.n.NightscapeFamiliar;
import com.github.laxika.magicalvibes.cards.p.PhyrexianTyranny;
import com.github.laxika.magicalvibes.cards.s.SunscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.ThornscapeFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeRoutes.class, SunscapeFamiliar.class, NightscapeFamiliar.class, ThornscapeFamiliar.class,
        PhyrexianTyranny.class, CavernHarpy.class})
class EscapeRoutesTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns a white creature you control to its owner's hand")
    void returnsWhiteCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sunscape Familiar");
        harness.assertInHand(player1, "Sunscape Familiar");
    }

    @Test
    @DisplayName("Ability returns a black creature you control to its owner's hand")
    void returnsBlackCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightscape Familiar");
        harness.assertInHand(player1, "Nightscape Familiar");
    }

    @Test
    @DisplayName("Cannot target a creature of another color")
    void cannotTargetCreatureOfAnotherColor() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThornscapeFamiliar());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or black creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's white creature")
    void cannotTargetOpponentsCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunscapeFamiliar());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or black creature you control");
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreaturePermanent() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhyrexianTyranny());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or black creature you control");
    }

    @Test
    @DisplayName("A blue and black creature is a legal target")
    void returnsMulticoloredBlackCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CavernHarpy());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cavern Harpy");
        harness.assertInHand(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("A creature controlled but not owned returns to its owner's hand")
    void returnsBorrowedCreatureToOwner() {
        addEscapeRoutes(player1);
        SunscapeFamiliar card = new SunscapeFamiliar();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sunscape Familiar");
        harness.assertNotInHand(player1, "Sunscape Familiar");
        harness.assertInHand(player2, "Sunscape Familiar");
    }

    @Test
    @DisplayName("The target must still be controlled at resolution")
    void doesNotReturnCreatureAfterLosingControl() {
        addEscapeRoutes(player1);
        SunscapeFamiliar card = new SunscapeFamiliar();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sunscape Familiar");
        harness.assertNotInHand(player1, "Sunscape Familiar");
        harness.assertNotInHand(player2, "Sunscape Familiar");
    }

    @Test
    @DisplayName("The ability resolves after Escape Routes leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EscapeRoutes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, source));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Escape Routes");
        harness.assertNotOnBattlefield(player1, "Sunscape Familiar");
        harness.assertInHand(player1, "Sunscape Familiar");
    }

    @Test
    @DisplayName("Escape Routes can activate repeatedly without tapping")
    void canActivateTwiceInResponse() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EscapeRoutes());
        Permanent white = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar());
        source.tap();
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, white.getId());
        harness.activateAbility(player1, 0, null, black.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sunscape Familiar");
        harness.assertInHand(player1, "Nightscape Familiar");
        harness.assertNotOnBattlefield(player1, "Sunscape Familiar");
        harness.assertNotOnBattlefield(player1, "Nightscape Familiar");
    }

    @Test
    @DisplayName("Spell cost reduction does not reduce the ability's cost")
    void requiresFullActivationCost() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sunscape Familiar");
        harness.assertNotInHand(player1, "Sunscape Familiar");
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required blue mana")
    void requiresBlueMana() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sunscape Familiar");
        harness.assertNotInHand(player1, "Sunscape Familiar");
    }

    private void addEscapeRoutes(Player player) {
        harness.addToBattlefield(player, new EscapeRoutes());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
