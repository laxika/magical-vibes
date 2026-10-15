package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerrowReejerey.class, DeeptreadMerrow.class, HillcomberGiant.class, Island.class, MerrowCommerce.class})
class MerrowReejereyTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk creatures you control get +1/+1")
    void buffsOwnMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player1, new DeeptreadMerrow());

        Permanent merrow = findPermanent(player1, "Deeptread Merrow");
        assertThat(gqs.getEffectivePower(gd, merrow)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merrow)).isEqualTo(2);
    }

    @Test
    @DisplayName("Merrow Reejerey does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MerrowReejerey());

        Permanent reejerey = findPermanent(player1, "Merrow Reejerey");
        assertThat(gqs.getEffectivePower(gd, reejerey)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reejerey)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff Merfolk creatures controlled by an opponent")
    void doesNotBuffOpponentsMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player2, new DeeptreadMerrow());

        Permanent merrow = findPermanent(player2, "Deeptread Merrow");
        assertThat(gqs.getEffectivePower(gd, merrow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merrow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.addToBattlefield(player1, new HillcomberGiant());

        Permanent giant = findPermanent(player1, "Hillcomber Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a Merfolk spell requires a target before the optional resolution choice")
    void merfolkCastRequiresTargetBeforeMayChoice() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Accepting taps an untapped target permanent")
    void acceptTapsUntappedTarget() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        UUID giantId = giant.getId();
        assertThat(giant.isTapped()).isFalse();

        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, giantId);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Merrow Reejerey"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting can tap an untapped noncreature permanent")
    void acceptTapsUntappedNoncreaturePermanent() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        UUID islandId = island.getId();

        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting untaps a tapped target permanent")
    void acceptUntapsTappedTarget() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        UUID giantId = giant.getId();
        giant.tap();

        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining does not tap or untap anything")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, giant.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Merrow Reejerey"));
        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-Merfolk spell does not trigger Merrow Reejerey")
    void nonMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player1, List.of(new HillcomberGiant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An opponent casting a Merfolk spell does not trigger Merrow Reejerey")
    void opponentMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player2, List.of(new DeeptreadMerrow()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Merrow Reejerey"));
    }

    @Test
    @DisplayName("A noncreature Merfolk spell also triggers the ability")
    void kindredEnchantmentSpellTriggers() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        harness.setHand(player1, List.of(new MerrowCommerce()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, findPermanent(player1, "Merrow Reejerey").getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("The controller can decline after the target becomes tapped in response")
    void canDeclineAfterTargetChangesBeforeResolution() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new DeeptreadMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        target.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Reejerey without another Reejerey on the battlefield does not trigger itself")
    void doesNotTriggerFromItsOwnCast() {
        harness.setHand(player1, List.of(new MerrowReejerey()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Merrow Reejerey");
    }

    @Test
    @DisplayName("Merrow Commerce is not a creature and does not receive the boost")
    void doesNotBoostNoncreatureMerfolk() {
        harness.addToBattlefield(player1, new MerrowReejerey());
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());

        assertThat(gqs.getEffectivePower(gd, commerce)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, commerce)).isZero();
    }

    @Test
    @DisplayName("Two Reejereys boost each other and their bonuses stack")
    void multipleReejereysStackTheirBoosts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MerrowReejerey());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MerrowReejerey());
        Permanent merrow = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, merrow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, merrow)).isEqualTo(3);
    }
}
