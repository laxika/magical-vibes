package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrivTheObligator.class, ZulaportCutthroat.class, JaceBeleren.class})
class ScrivTheObligatorTest extends BaseCardTest {

    @Test
    void entersWithWhiteContractAttachedToOpponentCreature() {
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);

        Permanent contract = findPermanent(player1, "Contract");
        assertThat(contract.getCard().isAura()).isTrue();
        assertThat(contract.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(contract.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void contractMakesCreatureControllerLoseLifeWhenItAttacksAuraController() {
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void contractMakesControllerLoseLifeWhenCreatureAttacksTheirPlaneswalker() {
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTargetAnOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new ZulaportCutthroat());
        harness.setHand(player1, List.of(new ScrivTheObligator()));
        addScrivMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void contractAttackTriggerIsControlledByAuraController() {
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);

        declareAttackersAt(player2, target, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
    }

    @Test
    void attackingScrivCreatesAnotherContract() {
        Permanent target = addCreatureReady(player2, new ZulaportCutthroat());
        castScriv(target);
        Permanent scriv = findPermanent(player1, "Scriv, the Obligator");
        scriv.setSummoningSick(false);

        declareAttackersAt(player1, scriv, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Contract")))
                .hasSize(2)
                .allSatisfy(p -> assertThat(p.getAttachedTo()).isEqualTo(target.getId()));
    }

    private void castScriv(Permanent target) {
        harness.setHand(player1, List.of(new ScrivTheObligator()));
        addScrivMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addScrivMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void declareAttackersAt(com.github.laxika.magicalvibes.model.Player attackerController,
                                    Permanent attacker, java.util.UUID attackTarget) {
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker);
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd, attackerController, List.of(attackerIndex), Map.of(attackerIndex, attackTarget)));
    }

}
