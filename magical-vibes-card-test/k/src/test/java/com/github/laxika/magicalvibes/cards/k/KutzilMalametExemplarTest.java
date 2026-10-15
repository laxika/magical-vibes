package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KutzilMalametExemplar.class, GrizzlyBears.class, Shock.class})
class KutzilMalametExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot cast spells during Kutzil's controller's turn")
    void opponentsCannotCastDuringControllerTurn() {
        Permanent kutzil = harness.addToBattlefieldAndReturn(player1, new KutzilMalametExemplar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, kutzil.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Combat damage from an unmodified creature does not draw")
    void unmodifiedCreatureDoesNotDraw() {
        addKutzil();
        addReadyAttacker();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Combat damage from a creature with greater power than base power draws")
    void modifiedCreatureDraws() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Multiple qualifying creatures cause only one draw")
    void multipleQualifyingCreaturesDrawOnce() {
        addKutzil();
        Permanent firstAttacker = addReadyAttacker();
        firstAttacker.setPowerModifier(1);
        Permanent secondAttacker = addReadyAttacker();
        secondAttacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).doesNotContain(nextCard);
    }

    @Test
    void opponentCanCastDuringTheirOwnTurn() {
        Permanent kutzil = addKutzil();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, kutzil.getId());

        assertThat(kutzil.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void controllerCanCastDuringTheirOwnTurn() {
        addKutzil();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void mixedQualifyingAndUnmodifiedAttackersDrawOnce() {
        addKutzil();
        addReadyAttacker();
        addReadyAttacker().setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).doesNotContain(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void kutzilCanTriggerFromItsOwnCombatDamage() {
        Permanent kutzil = addKutzil();
        kutzil.setSummoningSick(false);
        kutzil.setAttacking(true);
        kutzil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void toughnessIncreaseAloneDoesNotDraw() {
        addKutzil();
        addReadyAttacker().setToughnessModifier(2);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void increasedBasePowerAloneDoesNotDraw() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        attacker.setBasePowerOverride(4);
        attacker.setBaseToughnessOverride(4);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void powerIncreaseAboveOverriddenBasePowerDraws() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        attacker.setBasePowerOverride(1);
        attacker.setBaseToughnessOverride(1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void drawDoesNotRecheckPowerWhenTriggerResolves() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        attacker.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void doubleStrikeDrawsInEachDamageStep() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        attacker.getGrantedKeywords().add(Keyword.DOUBLE_STRIKE);
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Card thirdCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard, secondCard).doesNotContain(thirdCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
    }

    @Test
    void opposingBoostedCreatureDoesNotDrawForKutzilController() {
        addKutzil();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addKutzil() {
        return harness.addToBattlefieldAndReturn(player1, new KutzilMalametExemplar());
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }
}
