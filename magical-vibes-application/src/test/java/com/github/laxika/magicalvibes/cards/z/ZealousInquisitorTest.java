package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.r.RiptideSurvivor;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZealousInquisitor.class, AvenLiberator.class, RiptideSurvivor.class, SparkSpray.class})
class ZealousInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Each activation redirects one damage to its own chosen creature")
    void multipleActivationsRedirectToEachChosenCreature() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent firstDestination = addCreatureReady(player2, new AvenLiberator());
        Permanent secondDestination = addCreatureReady(player2, new AvenLiberator());

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, firstDestination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, secondDestination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SparkSpray(), new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, inquisitor.getId());
        harness.castAndResolveInstant(player2, 0, inquisitor.getId());

        assertThat(inquisitor.getMarkedDamage()).isZero();
        assertThat(firstDestination.getMarkedDamage()).isEqualTo(1);
        assertThat(secondDestination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncombat damage from a spell is redirected to the target creature")
    void redirectsNoncombatDamageFromSpell() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player2, new AvenLiberator());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, inquisitor.getId());

        assertThat(inquisitor.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the Inquisitor")
    void redirectsOnlyOneDamage() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player1, new AvenLiberator());
        Permanent attacker = addCreatureReady(player2, new RiptideSurvivor());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, attacker)));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, inquisitor), indexOf(player2, attacker))));
        resolveCombat(player2);

        // 1 of the 2 combat damage is redirected to the destination; 1 remains on the Inquisitor
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(inquisitor.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player2, new AvenLiberator());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, inquisitor.getId());

        assertThat(inquisitor.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("No shield is installed when the protected creature leaves before the ability resolves")
    void noShieldWhenInquisitorLeavesBeforeResolution() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player2, new AvenLiberator());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());

        gd.playerBattlefields.get(player1.getId()).remove(inquisitor);
        Permanent replacement = addCreatureReady(player1, new ZealousInquisitor());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, replacement.getId());

        assertThat(replacement.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature with protection from white cannot be chosen as the redirect destination")
    void cannotTargetCreatureWithProtectionFromWhite() {
        Permanent protectedCreature = addCreatureReady(player2, new AvenLiberator());
        harness.setHand(player2, List.of(new AvenLiberator()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();

        Permanent faceDownLiberator = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.turnFaceUp(player2, indexOf(player2, faceDownLiberator));
        harness.handlePermanentChosen(player2, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "WHITE");

        assertThat(gqs.hasProtectionFrom(gd, protectedCreature, CardColor.WHITE)).isTrue();

        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, inquisitor), null, protectedCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only a creature can be chosen as the redirect destination")
    void cannotTargetPlayer() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, inquisitor), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No redirect occurs when the target creature leaves before the ability resolves")
    void noRedirectWhenDestinationLeavesBeforeResolution() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player2, new AvenLiberator());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, inquisitor), null, destination.getId());

        gd.playerBattlefields.get(player2.getId()).remove(destination);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, inquisitor.getId());

        assertThat(inquisitor.getMarkedDamage()).isEqualTo(1);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
