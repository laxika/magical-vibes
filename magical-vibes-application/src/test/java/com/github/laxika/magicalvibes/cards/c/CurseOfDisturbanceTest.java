package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarettiScrapSavant;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfDisturbance.class, SolemnSimulacrum.class, DarettiScrapSavant.class})
class CurseOfDisturbanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie for the curse controller and the attacking player")
    void createsZombieForBothPlayersWhenEnchantedPlayerIsAttacked() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new SolemnSimulacrum());
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(findTokens(player1)).hasSize(1);
        assertThat(findTokens(player2)).hasSize(1);
        assertThat(findTokens(player1).getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(findTokens(player1).getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(findTokens(player1).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(findTokens(player1).getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(findTokens(player1).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when a planeswalker is attacked")
    void doesNotTriggerForPlaneswalkerAttack() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new SolemnSimulacrum());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new DarettiScrapSavant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), java.util.Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(findTokens(player1)).isEmpty();
        assertThat(findTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("The curse controller gets only one Zombie when attacking the enchanted player")
    void controllerAttackingGetsOnlyOneZombie() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfDisturbance());
        curse.setAttachedTo(player2.getId());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findTokens(player1)).hasSize(1);
        assertThat(findTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("An opponent with no remaining attackers gets no Zombie on resolution")
    void opponentNoLongerAttackingGetsNoZombie() {
        placeCurseOnPlayer1();
        Permanent attacker = addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerHands.get(player2.getId()).add(attacker.getCard());
        resolveAllTriggers();

        assertThat(findTokens(player1)).hasSize(1);
        assertThat(findTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("The trigger resolves after the curse leaves the battlefield")
    void triggerSurvivesCurseLeaving() {
        placeCurseOnPlayer1();
        Permanent curse = findPermanent(player1, "Curse of Disturbance");
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerHands.get(player1.getId()).add(curse.getCard());
        resolveAllTriggers();

        assertThat(findTokens(player1)).hasSize(1);
        assertThat(findTokens(player2)).hasSize(1);
    }

    @Test
    @DisplayName("Attacking a player other than the enchanted player creates no Zombies")
    void attackingUnenchantedPlayerDoesNotTrigger() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfDisturbance());
        curse.setAttachedTo(player2.getId());
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findTokens(player1)).isEmpty();
        assertThat(findTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("The Aura can be cast enchanting an opponent")
    void castsEnchantingOpponent() {
        harness.setHand(player1, List.of(new CurseOfDisturbance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of Disturbance").getAttachedTo())
                .isEqualTo(player2.getId());
    }

    private void placeCurseOnPlayer1() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfDisturbance());
        curse.setAttachedTo(player1.getId());
    }

    private List<Permanent> findTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
