package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ScrivTheObligator.class, GrizzlyBears.class})
class ScrivTheObligatorTest extends BaseCardTest {

    @Test
    void entersWithWhiteContractAttachedToOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castScriv(target);

        Permanent contract = findPermanent(player1, "Contract");
        assertThat(contract.getCard().isAura()).isTrue();
        assertThat(contract.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(contract.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void contractBoostsEnchantedCreatureWhenItAttacksAnOpponent() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castScriv(target);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void contractMakesControllerLoseLifeWhenCreatureAttacksTheirPlaneswalker() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castScriv(target);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        harness.setLife(player2, 20);

        declareAttackersAt(player2, target, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void canOnlyTargetAnOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScrivTheObligator()));
        addScrivMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
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

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
