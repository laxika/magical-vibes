package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DamnablePact;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshlandBloodcaster.class, GrizzlyBears.class, DamnablePact.class, SphereOfResistance.class})
class MarshlandBloodcasterTest extends BaseCardTest {

    @Test
    void letsTheNextSpellPayLifeEqualToItsManaValue() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void permissionIsConsumedByTheNextSpell() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void payingManaConsumesThePermissionEvenWhenLifeWasAvailable() {
        resolveBloodcasterAbility();
        harness.setHand(player1, List.of(new MarshlandBloodcaster(), new MarshlandBloodcaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayMoreLifeThanThePlayerHasAndFailedCastDoesNotConsumePermission() {
        resolveBloodcasterAbility();
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new MarshlandBloodcaster()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.setLife(player1, 6);
        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    @Test
    void permissionExpiresAtTheEndOfTheTurn() {
        resolveBloodcasterAbility();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MarshlandBloodcaster()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifePaymentAllowsAnXSpellWithXZero() {
        resolveBloodcasterAbility();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DamnablePact()));

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void lifePaymentCannotCastAnXSpellWithNonzeroX() {
        resolveBloodcasterAbility();
        harness.setHand(player1, List.of(new DamnablePact()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void lifePaymentStillSpendsManaForCostIncreases() {
        resolveBloodcasterAbility();
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MarshlandBloodcaster()));

        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void activationCannotBeUsedWhileSummoningSick() {
        harness.addToBattlefield(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activationTapsBloodcasterAndCannotBeRepeatedWithoutUntapping() {
        resolveBloodcasterAbility();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleResolvedActivationsAllApplyToTheSameNextSpell() {
        resolveBloodcasterAbility();
        addCreatureReady(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new MarshlandBloodcaster(), new MarshlandBloodcaster()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveBloodcasterAbility() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
