package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfCho;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushwoodHerbalist.class, RushwoodDryad.class, Forest.class, FountainOfCho.class})
class RushwoodHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and regenerates a target creature")
    void discardsAndRegeneratesTargetCreature() {
        Permanent herbalist = addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(herbalist.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {G}")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent herbalist = addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        herbalist.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new RushwoodHerbalist());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new FountainOfCho());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can discard a nonland card to regenerate itself")
    void canDiscardNonlandAndTargetItself() {
        Permanent herbalist = addCreatureReady(player1, new RushwoodHerbalist());
        harness.setHand(player1, List.of(new RushwoodDryad()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, herbalist.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Rushwood Dryad");
        assertThat(herbalist.isTapped()).isTrue();
        assertThat(herbalist.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(herbalist.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent herbalist = harness.addToBattlefieldAndReturn(player1, new RushwoodHerbalist());
        herbalist.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The shield replaces destruction, taps the creature and removes damage and combat status")
    void shieldReplacesDestruction() {
        addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        target.setMarkedDamage(1);
        target.setAttacking(true);
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, target, false);

        harness.assertOnBattlefield(player2, "Rushwood Dryad");
        harness.assertNotInGraveyard(player2, "Rushwood Dryad");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.isAttacking()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getRegenerationShield()).isZero();

        harness.getPermanentRemovalService().tryDestroyPermanent(gd, target, false);

        harness.assertNotOnBattlefield(player2, "Rushwood Dryad");
        harness.assertInGraveyard(player2, "Rushwood Dryad");
    }

    @Test
    @CardUsed({Vendetta.class})
    @DisplayName("A regeneration shield cannot save a creature from Vendetta")
    void cannotRegenerateWhenDestructionForbidsIt() {
        addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Rushwood Dryad");
        harness.assertInGraveyard(player2, "Rushwood Dryad");
    }

    @Test
    @DisplayName("Removing the source does not stop the activated ability")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent herbalist = addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, herbalist, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rushwood Herbalist");
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target receives no shield and the paid costs are not refunded")
    void targetLeavingBattlefieldDoesNotRefundCosts() {
        Permanent herbalist = addCreatureReady(player1, new RushwoodHerbalist());
        Permanent target = addCreatureReady(player2, new RushwoodDryad());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, target, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Rushwood Dryad");
        assertThat(herbalist.isTapped()).isTrue();
        assertThat(herbalist.getRegenerationShield()).isZero();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
