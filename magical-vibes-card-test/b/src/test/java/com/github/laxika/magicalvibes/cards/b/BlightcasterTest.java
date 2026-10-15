package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blightcaster.class, GloriousAnthem.class, GrizzlyBears.class, HillGiant.class, Boomerang.class})
class BlightcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment lets the controller give target creature -2/-2")
    void enchantmentCastShrinksTargetCreature() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities(); // resolve the triggered ability
        harness.handleMayAbilityChosen(player1, true);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may choice leaves the target unchanged")
    void decliningLeavesTargetUnchanged() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature reduced to 0 toughness dies")
    void lethalShrinkKillsCreature() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a non-enchantment spell does not trigger the ability")
    void nonEnchantmentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's enchantment spell does not trigger Blightcaster")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Blightcaster can target itself before the enchantment resolves")
    void canTargetItself() {
        harness.addToBattlefield(player1, new Blightcaster());
        UUID casterId = harness.getPermanentId(player1, "Blightcaster");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, casterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent caster = findPermanent(player1, "Blightcaster");
        assertThat(gqs.getEffectivePower(gd, caster)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, caster)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The -2/-2 effect expires at end of turn")
    void shrinkExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing Blightcaster does not stop its triggered ability")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID casterId = harness.getPermanentId(player1, "Blightcaster");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.castAndResolveInstant(player2, 0, casterId);
        harness.assertInHand(player1, "Blightcaster");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("A trigger with a removed target does not offer the may choice")
    void removedTargetMakesTriggerFailToResolve() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }
}
