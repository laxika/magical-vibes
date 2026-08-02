package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarrukApexPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 destroys another planeswalker")
    void plusOneDestroysPlaneswalker() {
        Permanent garruk = addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new LilianaOfTheVeil());
        Permanent liliana = findPermanent(player2, "Liliana of the Veil");
        liliana.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, liliana.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana of the Veil");
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 cannot target Garruk himself")
    void plusOneCannotTargetSelf() {
        Permanent garruk = addReadyGarruk(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, garruk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 cannot target a creature")
    void plusOneCannotTargetCreature() {
        addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 creates a 3/3 Beast token with deathtouch")
    void plusOneCreatesBeastToken() {
        Permanent garruk = addReadyGarruk(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 destroys a creature and gains life equal to its toughness")
    void minusThreeDestroysAndGainsLife() {
        Permanent garruk = addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 gives the emblem to the target opponent, not to Garruk's controller")
    void minusEightGivesEmblemToOpponent() {
        addReadyGarruk(player1, 9);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Creatures attacking the emblem's controller get +5/+5 and trample")
    void emblemBoostsAttackingCreature() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        Permanent bears = addReadyAttacker(player1);
        declareAttack(player1, bears, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The emblem does not trigger when a creature attacks a planeswalker instead")
    void emblemDoesNotTriggerOnPlaneswalkerAttack() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new LilianaOfTheVeil());
        Permanent liliana = findPermanent(player2, "Liliana of the Veil");
        liliana.setCounterCount(CounterType.LOYALTY, 3);

        Permanent bears = addReadyAttacker(player1);
        declareAttack(player1, bears, liliana.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The emblem does not boost creatures attacking the player without it")
    void emblemDoesNotBoostAttacksOnOtherPlayer() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        Permanent bears = addReadyAttacker(player2);
        declareAttack(player2, bears, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private void declareAttack(Player attackingPlayer, Permanent attacker, java.util.UUID defenderId) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackingPlayer, List.of(index), Map.of(index, defenderId));
    }

    private Permanent addReadyAttacker(Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        Permanent bears = findPermanent(player, "Grizzly Bears");
        bears.setSummoningSick(false);
        return bears;
    }

    private Permanent addReadyGarruk(Player player, int loyalty) {
        Permanent perm = new Permanent(new GarrukApexPredator());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
