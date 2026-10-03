package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.g.Guttersnipe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidsDeliverance.class, DrudgeBeetle.class, Guttersnipe.class, AnnihilatingFire.class, CallOfTheConclave.class})
class DruidsDeliveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage that would be dealt to the caster is prevented")
    void preventsCombatDamageToController() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Populate copies the only creature token its controller controls")
    void populateCopiesTheOnlyCreatureToken() {
        harness.addToBattlefield(player2, soldierToken());
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Populate does nothing without a creature token")
    void populateDoesNothingWithoutACreatureToken() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player2, "Drudge Beetle")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage from an attacking creature is not prevented")
    void doesNotPreventTriggeredDamageFromAnAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Guttersnipe());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0);

        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Populate chooses a controlled token and does not copy its counters or tapped state")
    void populateChoosesBetweenRealCreatureTokens() {
        harness.setHand(player1, List.of(new CallOfTheConclave(), new CallOfTheConclave(), new DruidsDeliverance()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castAndResolveSorcery(player1, 0, List.of());
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId());
        Permanent chosen = tokens.getFirst();
        chosen.setTapped(true);
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        List<UUID> originalIds = tokens.stream().map(Permanent::getId).toList();

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !originalIds.contains(p.getId())).findFirst().orElseThrow();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's creature token")
    void populateIgnoresOpponentsToken() {
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addAttacker(Player attackerController, Player defender) {
        Permanent perm = harness.addToBattlefieldAndReturn(attackerController, new DrudgeBeetle());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
