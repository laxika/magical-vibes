package com.github.laxika.magicalvibes.cards.k;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({KindleTheCarnage.class, DrudgeSkeletons.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class KindleTheCarnageTest extends BaseCardTest {

    @Test
    @DisplayName("Discards at random and deals that card's mana value to each creature")
    void discardsAndDamagesEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KindleTheCarnage(), new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("A mana-value-zero discard deals no damage")
    void zeroManaValueDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KindleTheCarnage(), new Ornithopter()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May repeat the discard and damage process")
    void mayRepeatProcess() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the repeat ends the process")
    void declineRepeatEndsProcess() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when there is no card left to discard")
    void noCardsToDiscardDealsNoDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new KindleTheCarnage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creatures with lethal damage remain until all repeated iterations finish")
    void lethalDamageWaitsUntilProcessFinishes() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining a repeat lets creatures with lethal damage die")
    void decliningRepeatFinishesLethalDamageProcess() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A zero-mana-value discard still allows repeated iterations")
    void canRepeatAfterZeroManaValueDiscard() {
        var creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new Ornithopter(), new Ornithopter(), new Ornithopter()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Ornithopter).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("One regeneration shield saves a creature from every damage iteration")
    void oneRegenerationShieldSavesFromRepeatedDamage() {
        var skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.setHand(player1, List.of(
                new KindleTheCarnage(), new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);
        assertThat(skeletons.isTapped()).isFalse();
        int firstDamage = gd.playerHands.get(player1.getId()).getFirst() instanceof HillGiant ? 2 : 4;
        assertThat(skeletons.getMarkedDamage()).isEqualTo(firstDamage);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(skeletons.getRegenerationShield()).isZero();
        assertThat(skeletons.getMarkedDamage()).isZero();
        assertThat(skeletons.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
