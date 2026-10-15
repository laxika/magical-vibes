package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bullwhip;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamanEnKor.class, Bullwhip.class, HonorGuard.class, Shock.class})
class ShamanEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent destination = addCreatureReady(player1, new ShamanEnKor());

        harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, shaman.getId());

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The free ability cannot target an opponent's creature as the destination")
    void freeAbilityCannotTargetOpponentsCreature() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new HonorGuard());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, shaman), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free ability can target Shaman en-Kor itself")
    void freeAbilityCanTargetItself() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent attacker = addCreatureReady(player2, new HonorGuard());

        harness.activateAbility(player1, indexOf(player1, shaman), null, shaman.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, shaman), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The paid ability redirects the chosen source's next damage to Shaman en-Kor")
    void redirectsChosenSourceNextDamageToSelf() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent protectedCreature = addCreatureReady(player2, new ShamanEnKor());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bullwhip.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, bullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("The paid ability redirects only the chosen source's next damage event")
    void paidAbilityRedirectsOnlyNextDamageEvent() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent firstBullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent secondBullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent protectedCreature = addCreatureReady(player2, new ShamanEnKor());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstBullwhip.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, indexOf(player1, firstBullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondBullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The paid ability redirects combat damage from the chosen source")
    void redirectsChosenSourceCombatDamageToSelf() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent protectedCreature = addCreatureReady(player1, new HonorGuard());
        Permanent attacker = addCreatureReady(player2, new HonorGuard());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, protectedCreature), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("The paid ability can choose an instant spell on the stack as the source")
    void redirectsDamageFromInstantSpellOnStack() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent protectedCreature = addCreatureReady(player2, new ShamanEnKor());
        Shock shock = new Shock();

        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(2);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple free activations can redirect more damage than the destination's toughness")
    void multipleFreeActivationsRedirectEntireDamageEvent() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent destination = addCreatureReady(player1, new HonorGuard());

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
            harness.passBothPriorities();
        }

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, shaman.getId());

        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Shaman en-Kor");
        harness.assertInGraveyard(player1, "Honor Guard");
    }

    @Test
    @DisplayName("Damage redirected by the paid ability can be redirected again by the free ability")
    void paidRedirectionCanChainIntoFreeRedirection() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent destination = addCreatureReady(player1, new ShamanEnKor());
        Permanent protectedCreature = addCreatureReady(player2, new ShamanEnKor());
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());

        harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bullwhip.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, bullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(shaman.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirected combat damage can be redirected again by the free ability")
    void combatRedirectionCanChainIntoFreeRedirection() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent destination = addCreatureReady(player1, new ShamanEnKor());
        Permanent protectedCreature = addCreatureReady(player1, new HonorGuard());
        Permanent attacker = addCreatureReady(player2, new HonorGuard());

        harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, protectedCreature), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(shaman.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from a different source does not consume the paid redirection")
    void differentSourceDoesNotConsumePaidRedirection() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent protectedCreature = addCreatureReady(player2, new ShamanEnKor());
        Permanent chosenBullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent otherBullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenBullwhip.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, indexOf(player1, otherBullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();
        assertThat(shaman.getMarkedDamage()).isZero();
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, indexOf(player1, chosenBullwhip), null, protectedCreature.getId());
        harness.passBothPriorities();
        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A self-redirection does not prevent another shield from redirecting that damage")
    void selfRedirectionStillAllowsAnotherShieldToApply() {
        Permanent shaman = addCreatureReady(player1, new ShamanEnKor());
        Permanent destination = addCreatureReady(player1, new HonorGuard());

        harness.activateAbility(player1, indexOf(player1, shaman), null, shaman.getId());
        harness.passBothPriorities();
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
            harness.passBothPriorities();
        }

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, shaman.getId());

        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Honor Guard");
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
