package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NetterEnDal;
import com.github.laxika.magicalvibes.cards.n.NobleStand;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolaWarrior.class, NetterEnDal.class, NobleStand.class, SealOfFire.class})
class BolaWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes the target creature unable to block and discards a card")
    void abilityPreventsBlocking() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        assertThat(bls.canBlock(gd, target)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Netter en-Dal");
    }

    @Test
    @DisplayName("Can't-block restriction wears off at end of turn")
    void cantBlockWearsOff() {
        addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Activating starts the discard-cost choice")
    void activatingStartsDiscardChoice() {
        addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the ability with an empty hand")
    void cannotActivateWithEmptyHand() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(warrior.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new NobleStand());
        UUID nobleStandId = harness.getPermanentId(player2, "Noble Stand");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nobleStandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(warrior.isTapped()).isFalse();
        harness.assertInHand(player1, "Netter en-Dal");
    }

    @Test
    @DisplayName("Cannot activate the ability while Bola Warrior is tapped")
    void cannotActivateWhileTapped() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        warrior.tap();
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Netter en-Dal");
    }

    @Test
    @DisplayName("Cannot activate the ability while Bola Warrior has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BolaWarrior());
        harness.setHand(player1, List.of(new NetterEnDal()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Netter en-Dal");
    }

    @Test
    @DisplayName("Ability can target a friendly creature and discard a noncreature card")
    void canTargetFriendlyCreatureAndDiscardEnchantment() {
        addCreatureReady(player1, new BolaWarrior());
        Permanent target = addCreatureReady(player1, new NetterEnDal());
        Permanent unaffected = addCreatureReady(player2, new NetterEnDal());
        harness.setHand(player1, List.of(new NobleStand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Noble Stand");
        assertThat(bls.canBlock(gd, target)).isTrue();
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();
        assertThat(bls.canBlock(gd, unaffected)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after Bola Warrior is killed in response")
    void abilityResolvesWithoutSource() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        Permanent target = addCreatureReady(player2, new NetterEnDal());
        harness.addToBattlefield(player2, new SealOfFire());
        harness.setHand(player1, List.of(new NobleStand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player2, 1, 0, null, warrior.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bola Warrior");
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Losing the target does not refund the activation costs or affect another creature")
    void removedTargetDoesNotRefundCosts() {
        Permanent warrior = addCreatureReady(player1, new BolaWarrior());
        harness.addToBattlefield(player1, new SealOfFire());
        Permanent target = addCreatureReady(player2, new NetterEnDal());
        Permanent unaffected = addCreatureReady(player2, new NetterEnDal());
        harness.setHand(player1, List.of(new NobleStand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 1, 0, null, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Netter en-Dal");
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Noble Stand");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(bls.canBlock(gd, unaffected)).isTrue();
    }
}
