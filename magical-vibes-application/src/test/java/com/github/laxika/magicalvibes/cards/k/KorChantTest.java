package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DominatingLicid;
import com.github.laxika.magicalvibes.cards.o.OgreShaman;
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

@CardUsed({KorChant.class, OgreShaman.class, KillerWhale.class, DominatingLicid.class})
class KorChantTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects all damage from the chosen source to the other target creature")
    void redirectsDamageToOtherTargetCreature() {
        Permanent protectedCreature = addCreatureReady(player1, new KillerWhale());
        Permanent redirectCreature = addCreatureReady(player2, new KillerWhale());
        Permanent shaman = addCreatureReady(player1, new OgreShaman());
        castKorChant(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, shaman.getId());
        harness.setHand(player1, List.of(new KorChant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, shaman), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage from another source still reaches the protected creature")
    void doesNotRedirectDamageFromAnotherSource() {
        Permanent protectedCreature = addCreatureReady(player1, new KillerWhale());
        Permanent redirectCreature = addCreatureReady(player2, new KillerWhale());
        Permanent chosenSource = addCreatureReady(player1, new OgreShaman());
        Permanent otherSource = addCreatureReady(player1, new OgreShaman());
        castKorChant(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, chosenSource.getId());
        harness.setHand(player1, List.of(new KorChant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, otherSource), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Redirects multiple damage events from the chosen source this turn")
    void redirectsMultipleDamageEventsFromChosenSource() {
        Permanent protectedCreature = addCreatureReady(player1, new KillerWhale());
        Permanent redirectCreature = addCreatureReady(player2, new KillerWhale());
        Permanent shaman = addCreatureReady(player1, new OgreShaman());
        castKorChant(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, shaman.getId());
        harness.setHand(player1, List.of(new KorChant(), new KorChant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(player1, shaman), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, shaman), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Redirects combat damage from the chosen source to the other target creature")
    void redirectsCombatDamage() {
        Permanent protectedCreature = addCreatureReady(player1, new KillerWhale());
        Permanent redirectCreature = addCreatureReady(player2, new KillerWhale());
        Permanent attacker = addCreatureReady(player2, new KillerWhale());
        castKorChant(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, protectedCreature), indexOf(player2, attacker))));
        resolveCombat(player2);

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not redirect damage when the destination is no longer a creature")
    void doesNotRedirectToNonCreatureDestination() {
        Permanent protectedCreature = addCreatureReady(player1, new KillerWhale());
        Permanent redirectCreature = addCreatureReady(player2, new DominatingLicid());
        Permanent auraTarget = addCreatureReady(player2, new KillerWhale());
        Permanent shaman = addCreatureReady(player1, new OgreShaman());
        castKorChant(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, shaman.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, indexOf(player2, redirectCreature), null, auraTarget.getId());
        harness.passBothPriorities();
        assertThat(redirectCreature.getCard().isAura()).isTrue();

        harness.setHand(player1, List.of(new KorChant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, shaman), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires two different creature targets")
    void requiresDifferentCreatureTargets() {
        Permanent creature = addCreatureReady(player1, new KillerWhale());
        harness.setHand(player1, List.of(new KorChant()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the protected target to be a creature you control")
    void requiresProtectedTargetToBeControlledByCaster() {
        Permanent opponentCreature = addCreatureReady(player2, new KillerWhale());
        Permanent ownCreature = addCreatureReady(player1, new KillerWhale());
        harness.setHand(player1, List.of(new KorChant()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castKorChant(Permanent protectedCreature, Permanent redirectCreature) {
        harness.setHand(player1, List.of(new KorChant()));
        addCastMana();
        harness.castAndResolveInstant(player1, 0, List.of(protectedCreature.getId(), redirectCreature.getId()));
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
