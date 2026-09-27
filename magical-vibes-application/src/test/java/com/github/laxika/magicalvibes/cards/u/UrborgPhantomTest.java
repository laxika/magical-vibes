package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CinderShade;
import com.github.laxika.magicalvibes.cards.l.LightningDart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgPhantom.class, CinderShade.class, LightningDart.class})
class UrborgPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Urborg Phantom can't block")
    void cannotBlock() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());

        assertThat(bls.canBlock(gd, phantom)).isFalse();
    }

    @Test
    @DisplayName("Activating Urborg Phantom prevents combat damage dealt to and by it")
    void abilityPreventsCombatDamageBothWays() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());
        Permanent blocker = addCreatureReady(player1, new CinderShade());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(phantom.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Urborg Phantom");
    }

    @Test
    @DisplayName("The ability does not prevent combat damage dealt by other creatures")
    void abilityDoesNotPreventOtherCreaturesCombatDamage() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());
        Permanent otherAttacker = addCreatureReady(player2, new CinderShade());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        harness.assertLife(player1, 19);
        assertThat(phantom.getMarkedDamage()).isZero();
        assertThat(otherAttacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The combat-only ability does not prevent noncombat damage to Urborg Phantom")
    void abilityDoesNotPreventNoncombatDamage() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, phantom.getId());

        assertThat(phantom.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Urborg Phantom");
    }

    @Test
    @DisplayName("Without activating the ability Urborg Phantom deals and takes combat damage normally")
    void combatDamageIsNotPreventedByDefault() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());
        addCreatureReady(player1, new CinderShade());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(phantom.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Urborg Phantom");
        harness.assertInGraveyard(player1, "Cinder Shade");
    }
}
