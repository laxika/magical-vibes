package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RelicPutrescence;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.cards.w.WanderwineHub;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The MTGO-style "cancel casting" flow: mana-ability activations are recorded in
 * {@code GameData.revertableManaActivations} and {@code GameService.revertManaActivations}
 * undoes them — sources untap, the produced mana leaves the pool — as long as the mana
 * hasn't been spent.
 */
class ManaActivationRevertTest extends BaseCardTest {

    private static Card createGreenCreature() {
        Card card = new Card();
        card.setName("Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Reverting untaps tapped lands and drains the mana they produced")
    void revertUntapsLandsAndDrainsPool() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Reverting an activated mana ability (dual land) untaps it and removes the chosen color")
    void revertActivatedManaAbility() {
        harness.addToBattlefield(player1, new WanderwineHub());
        harness.ensurePriority(player1);

        // Second ability: {T}: Add {U}.
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spent mana is not reverted: after casting with one of two taps, only the unspent land untaps")
    void spentManaIsNotReverted() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.castCreature(player1, 0); // pays {G}, leaving 1 G floating

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream().filter(Permanent::isTapped).count()).isEqualTo(1);
        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Passing priority clears the revert log — nothing untaps afterwards")
    void passPriorityClearsRevertLog() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        assertThat(gd.revertableManaActivations).hasSize(1);

        harness.passPriority(player1);

        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Reverting untaps a Birds of Paradise and drains the color it was asked to choose")
    void revertUntapsAnyColorSource() {
        harness.addToBattlefield(player1, new BirdsOfParadise());
        Permanent birds = gd.playerBattlefields.get(player1.getId()).getFirst();
        birds.setSummoningSick(false);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        // The activation taps immediately but owes its mana until the color prompt is answered.
        assertThat(birds.isTapped()).isTrue();
        assertThat(gd.revertableManaActivations).isEmpty();

        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.revertableManaActivations).hasSize(1);

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(birds.isTapped()).isFalse();
        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("A Birds of Paradise activation does not poison earlier revertable taps")
    void anyColorSourceKeepsEarlierActivationsRevertable() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BirdsOfParadise());
        Permanent birds = gd.playerBattlefields.get(player1.getId()).get(1);
        birds.setSummoningSick(false);
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Reverting while the color prompt is still open is refused and drops the parked activation")
    void cannotRevertWhileColorChoiceIsOpen() {
        harness.addToBattlefield(player1, new BirdsOfParadise());
        Permanent birds = gd.playerBattlefields.get(player1.getId()).getFirst();
        birds.setSummoningSick(false);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);

        // The engine refuses actions while input is awaited, so the prompt must be answered first.
        assertThatThrownBy(() -> gs.revertManaActivations(gd, player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(birds.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Reverting with nothing recorded is a no-op")
    void revertWithoutActivationsIsNoOp() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A player without priority cannot revert, and the log is untouched")
    void revertRequiresPriority() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> gs.revertManaActivations(gd, player2))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.revertableManaActivations).hasSize(1);
    }

    @Test
    @DisplayName("A mana ability activated on its own puts its trigger on the stack and can no longer be reverted")
    void standaloneActivationWithTriggerIsCommitted() {
        harness.addToBattlefield(player1, new Forest());
        Permanent myr = enchantedSilverMyr();
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.revertableManaActivations).hasSize(1);

        gs.revertManaActivations(gd, player1);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forest.isTapped()).isFalse();
        assertThat(myr.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A mana ability activated to pay for a cast holds its trigger, and reverting drops it")
    void paymentActivationHoldsTriggerUntilReverted() {
        Permanent myr = enchantedSilverMyr();
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.tapPermanentToPayFor(player1, 0, ManaPaymentIntent.forCast(0, 0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        gs.revertManaActivations(gd, player1);

        assertThat(myr.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unspecified payment intent still holds the trigger until the spell is cast above it")
    void unspecifiedPaymentHoldsTriggerUntilCast() {
        enchantedSilverMyr();
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.tapPermanentToPayFor(player1, 0, ManaPaymentIntent.unspecified());
        harness.tapPermanentToPayFor(player1, 1, ManaPaymentIntent.unspecified());
        assertThat(gd.stack).isEmpty();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("An any-colour mana ability activated on its own puts its trigger on the stack once the colour is chosen")
    void standaloneAnyColorActivationCommitsAfterColorChoice() {
        Permanent manalith = enchantedManalith();
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "RED");

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.revertableManaActivations).isEmpty();

        gs.revertManaActivations(gd, player1);

        assertThat(manalith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A standalone any-colour activation leaves earlier payment taps revertable and their triggers held")
    void standaloneAnyColorActivationKeepsEarlierPaymentTapsHeld() {
        Permanent myr = enchantedSilverMyr();
        Permanent manalith = enchantedManalith();
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.tapPermanentToPayFor(player1, 0, ManaPaymentIntent.forCast(0, 0));
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        gs.revertManaActivations(gd, player1);

        assertThat(myr.isTapped()).isFalse();
        assertThat(manalith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An any-colour mana ability activated to pay for a cast holds its trigger past the colour choice")
    void paymentAnyColorActivationHoldsTriggerAfterColorChoice() {
        Permanent manalith = enchantedManalith();
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.activateAbilityToPayFor(player1, 0, ManaPaymentIntent.forCast(0, 0));
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        gs.revertManaActivations(gd, player1);

        assertThat(manalith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent enchantedManalith() {
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());
        manalith.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RelicPutrescence());
        aura.setAttachedTo(manalith.getId());
        return manalith;
    }

    private Permanent enchantedSilverMyr() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new SilverMyr());
        myr.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RelicPutrescence());
        aura.setAttachedTo(myr.getId());
        return myr;
    }
}
