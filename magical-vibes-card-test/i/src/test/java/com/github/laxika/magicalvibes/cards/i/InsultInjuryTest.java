package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({InsultInjury.class, Shock.class, GrizzlyBears.class, SerraAngel.class,
        Colossapede.class, GideonOfTheTrials.class})
class InsultInjuryTest extends BaseCardTest {

    @Test
    @DisplayName("Insult makes damage unpreventable and doubles controller sources this turn")
    void insultDoublesControllerDamageAndPreventsPrevention() {
        harness.setHand(player1, List.of(new InsultInjury(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
        assertThat(gd.controllerDamageDoublingsThisTurn.get(player1.getId())).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Shock 2 doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Insult");
    }

    @Test
    @DisplayName("Insult doubles combat damage from controlled creatures")
    void insultDoublesCombatDamage() {
        harness.setHand(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Insult does not double opponent's damage")
    void insultDoesNotDoubleOpponentDamage() {
        harness.setHand(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Opponent Shock still deals 2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Injury from graveyard deals 2 to creature and 2 to player then exiles")
    void injuryDamagesCreatureAndPlayerThenExiles() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4
        UUID serraId = serra.getId();
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, List.of(serraId, player2.getId()));
        harness.passBothPriorities();

        assertThat(serra.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Insult") || c.getName().equals("Injury"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Insult"));
    }

    @Test
    @DisplayName("Injury cannot use a player as the creature target")
    void injuryRejectsPlayerAsCreatureTarget() {
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoInsultsQuadrupleInjuryDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new InsultInjury(), new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castFlashback(player1, 0, List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player2, "Colossapede");
    }

    @Test
    void insultDoublesInjuryAndOverridesDamagePrevention() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent gideon = harness.enterBattlefieldAndReturn(player1, new GideonOfTheTrials());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castFlashback(player1, 0, List.of(creature.getId(), gideon.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Gideon of the Trials");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Insult"));
    }

    @Test
    void injuryRemovesTwoLoyaltyFromPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        Permanent gideon = harness.enterBattlefieldAndReturn(player2, new GideonOfTheTrials());
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0, List.of(creature.getId(), gideon.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void injuryStillDamagesPlayerWhenCreatureLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFlashback(player1, 0, List.of(creature.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Insult"));
    }

    @Test
    void injuryRejectsCreatureAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void injuryIsExiledWhenBothTargetsBecomeIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        Permanent gideon = harness.enterBattlefieldAndReturn(player2, new GideonOfTheTrials());
        harness.setGraveyard(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFlashback(player1, 0, List.of(creature.getId(), gideon.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(creature, gideon));

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Insult"));
    }

    @Test
    void insultDamageDoublingExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new InsultInjury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gqs.isDamagePreventable(gd)).isTrue();
    }
}
