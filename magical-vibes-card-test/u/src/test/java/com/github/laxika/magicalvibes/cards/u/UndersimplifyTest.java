package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiftOfDoom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Undersimplify.class, GrizzlyBears.class, MightOfOaks.class, TurnToFrog.class, GiftOfDoom.class, AinokTracker.class})
class UndersimplifyTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell whose controller can't pay {2}")
    void countersCreatureSpellWhenControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Undersimplify");
    }

    @Test
    @DisplayName("A creature spell whose controller pays perpetually gets -2/-0")
    void creatureSpellGetsPerpetualPowerReductionWhenControllerPays() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent resolvedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, resolvedBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, resolvedBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a noncreature spell, which gets no perpetual change")
    void canTargetNoncreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        Permanent resolvedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, resolvedBears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, resolvedBears)).isEqualTo(9);
    }

    @Test
    @DisplayName("The perpetual -2/-0 still applies after the base power and toughness are set")
    void perpetualReductionStillAppliesAfterBasePowerAndToughnessAreSet() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Undersimplify(), new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent frog = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters a creature spell whose controller declines to pay")
    void countersCreatureSpellWhenControllerDeclinesAffordablePayment() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a noncreature spell whose controller can't pay")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A face-down spell is a creature spell, so the card cast face down perpetually gets -2/-0")
    void faceDownSpellIsACreatureSpell() {
        GiftOfDoom gift = new GiftOfDoom();
        harness.setHand(player1, List.of(gift));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getLast().getTargetableId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.perpetualCardPowerToughnessModifiers.get(gift.getId()))
                .isEqualTo(new CardPowerToughnessModifier(-2, 0));
    }

    @Test
    @DisplayName("A face-down creature with the perpetual -2/-0 is a 2/2 until it is turned face up")
    void perpetualChangeAppliesOnlyOnceTurnedFaceUp() {
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Undersimplify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getLast().getTargetableId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        assertThat(tracker.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }
}
