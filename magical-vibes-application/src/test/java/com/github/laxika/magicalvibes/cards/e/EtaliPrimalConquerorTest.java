package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CorruptedConviction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtaliPrimalConqueror.class, EtaliPrimalSickness.class, Forest.class, GrizzlyBears.class, CorruptedConviction.class})
class EtaliPrimalConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling each library until a nonland and offers all found spells")
    void entersByExilingUntilNonlandAndOffersFoundSpells() {
        Forest player1Land = new Forest();
        GrizzlyBears player1Spell = new GrizzlyBears();
        Forest player2Land = new Forest();
        GrizzlyBears player2Spell = new GrizzlyBears();
        Forest player1Remainder = new Forest();
        Forest player2Remainder = new Forest();
        harness.setLibrary(player1, List.of(player1Land, player1Spell, player1Remainder));
        harness.setLibrary(player2, List.of(player2Land, player2Spell, player2Remainder));
        harness.setHand(player1, List.of(new EtaliPrimalConqueror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds())
                .containsExactlyInAnyOrder(player1Spell.getId(), player2Spell.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(player1Land, player1Spell, player2Land, player2Spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Remainder);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Remainder);
    }

    @Test
    @DisplayName("Transforms into Etali, Primal Sickness")
    void transformsIntoPrimalSickness() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(etali.getCard().getName()).isEqualTo("Etali, Primal Sickness");
        assertThat(etali.isTransformed()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Etali, Primal Sickness gives poison equal to combat damage dealt")
    void primalSicknessGivesPoisonEqualToCombatDamage() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        etali.setCard(etali.getOriginalCard().getBackFaceCard());
        etali.setTransformed(true);
        etali.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(11);
    }

    @Test
    @DisplayName("May cast both players' exiled creatures for free under Etali's controller")
    void castsBothPlayersSpellsForFree() {
        GrizzlyBears ownSpell = new GrizzlyBears();
        GrizzlyBears opponentsSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownSpell, new Forest()));
        harness.setLibrary(player2, List.of(opponentsSpell, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(ownSpell.getId(), opponentsSpell.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .containsExactly(ownSpell.getId(), opponentsSpell.getId());
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(ownSpell.getId(), opponentsSpell.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining all spells leaves them and the lands in exile")
    void mayDeclineAllSpells() {
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell, new Forest()));
        harness.setLibrary(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(land, spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An all-land library is exhausted without preventing the other player's spell")
    void allLandLibraryDoesNotPreventOtherPlayersSpell() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setLibrary(player2, List.of(spell, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(firstLand, secondLand, spell);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).contains(spell.getId());
    }

    @Test
    @DisplayName("Empty libraries complete the trigger without a cast prompt")
    void emptyLibrariesCompleteTrigger() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new EtaliPrimalConqueror());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Green mana pays the Phyrexian symbol without paying life")
    void transformsWithGreenManaWithoutLifePayment() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(etali.isTransformed()).isFalse();
        harness.passBothPriorities();
        assertThat(etali.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
        etali.setMarkedDamage(11);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(etali);
    }

    @Test
    @DisplayName("Transformation cannot be activated during combat")
    void transformationRequiresSorceryTiming() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(etali.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A free spell with a payable mandatory sacrifice cost can be cast")
    void offersPaymentOfMandatoryAdditionalCost() {
        CorruptedConviction spell = new CorruptedConviction();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(spell, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Poison uses damage actually dealt and remains fixed after power changes")
    void poisonUsesActualDamageSnapshot() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        etali.setPowerModifier(-6);
        etali.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 15);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        etali.setPowerModifier(0);
        resolveAllTriggers();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The front face deals ordinary combat damage without poison")
    void frontFaceDoesNotGivePoison() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        etali.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
