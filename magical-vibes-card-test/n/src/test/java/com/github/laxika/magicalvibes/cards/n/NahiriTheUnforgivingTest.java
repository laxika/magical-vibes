package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.h.HexgoldHalberd;
import com.github.laxika.magicalvibes.cards.m.MandibleJusticiar;
import com.github.laxika.magicalvibes.cards.r.ResistanceReunited;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahiriTheUnforgiving.class, MandibleJusticiar.class, ResistanceReunited.class,
        HexgoldHalberd.class, Clone.class})
class NahiriTheUnforgivingTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes the target attack a player rather than a planeswalker each combat")
    void plusOneRequiresAttackingAPlayer() {
        Permanent nahiri = addReadyNahiri(3);
        Permanent planeswalker = nahiri;
        Permanent creature = addCreatureReady(player2, new MandibleJusticiar());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(creatureIndex),
                Map.of(creatureIndex, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player2, List.of(creatureIndex),
                Map.of(creatureIndex, player1.getId()));
    }

    @Test
    @DisplayName("+1 rummages")
    void plusOneDiscardsThenDraws() {
        addReadyNahiri(3);
        Card discarded = new ResistanceReunited();
        Card kept = new MandibleJusticiar();
        Card drawn = new ResistanceReunited();
        harness.setHand(player1, List.of(discarded, kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("0 creates a hasty temporary copy from a qualifying graveyard card")
    void zeroCreatesHastyCopyAndExilesItAtNextEndStep() {
        addReadyNahiri(3);
        Card bears = new MandibleJusticiar();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        Permanent token = findPermanent(player1, "Mandible Justiciar");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("0 rejects a graveyard card whose mana value is not below loyalty")
    void zeroRejectsCardAtOrAboveLoyalty() {
        addReadyNahiri(2);
        Card expensive = new MandibleJusticiar();
        harness.setGraveyard(player1, List.of(expensive));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(expensive.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNahiri(int loyalty) {
        Permanent nahiri = harness.addToBattlefieldAndReturn(player1, new NahiriTheUnforgiving());
        nahiri.setCounterCount(CounterType.LOYALTY, loyalty);
        nahiri.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nahiri;
    }

    @Test
    void plusOneDrawsWithAnEmptyHand() {
        addReadyNahiri(3);
        Card drawn = new ResistanceReunited();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void firstPlusOneCanChooseNoTarget() {
        Permanent nahiri = addReadyNahiri(3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void zeroUsesLastKnownLoyaltyWhenNahiriLeaves() {
        Permanent nahiri = addReadyNahiri(3);
        Card creature = new MandibleJusticiar();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(creature.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nahiri);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(findPermanent(player1, "Mandible Justiciar").getCard().isToken()).isTrue();
    }

    @Test
    void zeroRechecksLoyaltyAtResolution() {
        Permanent nahiri = addReadyNahiri(3);
        Card creature = new MandibleJusticiar();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(creature.getId()));
        nahiri.setCounterCount(CounterType.LOYALTY, 2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(countPermanents(player1, "Mandible Justiciar")).isZero();
    }

    @Test
    void zeroRejectsOpponentsGraveyardAndNonCreatureNonEquipmentCards() {
        addReadyNahiri(3);
        Card opponentCreature = new MandibleJusticiar();
        Card instant = new ResistanceReunited();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroCopiesNonCreatureEquipment() {
        addReadyNahiri(3);
        Card equipment = new HexgoldHalberd();
        harness.setGraveyard(player1, List.of(equipment));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(equipment.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(equipment);
        Permanent token = findPermanent(player1, "Hexgold Halberd");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    void copyingNahirisTokenDoesNotCopyGrantedHaste() {
        addReadyNahiri(3);
        Card creature = new MandibleJusticiar();
        harness.setGraveyard(player1, List.of(creature));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Mandible Justiciar");

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent copy = findPermanents(player1, "Mandible Justiciar").stream()
                .filter(permanent -> !permanent.getId().equals(token.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isFalse();
    }

    @Test
    void compleatedEntersWithThreeLoyaltyWhenLifePaysForHybridSymbol() {
        harness.setHand(player1, List.of(new NahiriTheUnforgiving()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nahiri, the Unforgiving")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    void compleatedAcceptsRedManaWithoutLosingLoyaltyOrLife() {
        assertManaPaidEntry(2, 1);
    }

    @Test
    void compleatedAcceptsWhiteManaWithoutLosingLoyaltyOrLife() {
        assertManaPaidEntry(1, 2);
    }

    private void assertManaPaidEntry(int redMana, int whiteMana) {
        harness.setHand(player1, List.of(new NahiriTheUnforgiving()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.addMana(player1, ManaColor.WHITE, whiteMana);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nahiri, the Unforgiving")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

}
