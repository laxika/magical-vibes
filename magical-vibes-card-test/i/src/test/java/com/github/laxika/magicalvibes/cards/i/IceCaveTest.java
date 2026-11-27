package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MutagenicGrowth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IceCave.class, GaeasSkyfolk.class, Illuminate.class, AvenLiberator.class})
class IceCaveTest extends BaseCardTest {

    /** The payers have a mana source, so a "may pay" with an empty pool is still asked (CR 605.3a). */
    @BeforeEach
    void givePayersAManaSource() {
        giveManaSource(player1);
        giveManaSource(player2);
    }

    @Test
    @DisplayName("Another player may pay the spell's exact mana cost to counter it")
    void anotherPlayerMayPayExactManaCostToCounterSpell() {
        harness.addToBattlefield(player1, new IceCave());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GaeasSkyfolk(), "{G}{U}");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Gaea's Skyfolk");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @CardUsed({MutagenicGrowth.class, GrizzlyBears.class})
    @DisplayName("A Phyrexian symbol in the spell's mana cost can be paid with 2 life (CR 107.4f)")
    void phyrexianSymbolCanBePaidWithLife() {
        harness.addToBattlefield(player1, new IceCave());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MutagenicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Mutagenic Growth");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @CardUsed(BorosRecruit.class)
    @DisplayName("A hybrid symbol in the spell's mana cost can be paid with either of its colors (CR 107.4e)")
    void hybridSymbolCanBePaidWithEitherColor() {
        harness.addToBattlefield(player1, new IceCave());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BorosRecruit()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castCreature(player2, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Boros Recruit");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The spell's controller is not offered the payment")
    void spellControllerIsNotOfferedThePayment() {
        harness.addToBattlefield(player1, new IceCave());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GaeasSkyfolk(), "{G}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("A colorless payment cannot pay a spell's colored mana cost")
    void coloredManaCostMustBePaidWithTheRequiredColor() {
        harness.addToBattlefield(player1, new IceCave());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GaeasSkyfolk(), "{G}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The other player may pay when the Ice Cave controller casts a spell")
    void otherPlayerMayPayWhenControllerCastsSpell() {
        harness.addToBattlefield(player1, new IceCave());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GaeasSkyfolk(), "{G}{U}");
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Gaea's Skyfolk");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The payment uses the spell's chosen X value")
    void paymentUsesChosenXValue() {
        harness.addToBattlefield(player1, new IceCave());
        var target = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Illuminate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Illuminate");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A second Ice Cave trigger still offers payment after the spell is countered")
    void paymentIsStillOfferedAfterSpellLeavesStack() {
        harness.addToBattlefield(player1, new IceCave());
        harness.addToBattlefield(player2, new IceCave());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GaeasSkyfolk(), "{G}{U}");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Gaea's Skyfolk");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Payment excludes kicker costs even when the spell was kicked")
    void paymentExcludesKickerCost() {
        harness.addToBattlefield(player1, new IceCave());
        var target = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Illuminate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player2);
        gs.playCard(gd, player2, 0, 2, target.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of(), false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Illuminate");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A spell cast face down has no mana cost, which can't be paid, so nobody is asked")
    void faceDownSpellIsNotCountered() {
        harness.addToBattlefield(player1, new IceCave());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AvenLiberator()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(Permanent::isFaceDown);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }
}
