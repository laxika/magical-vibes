package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MageSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature deals damage equal to its power to the attacked player")
    void dealsPowerDamageToAttackedPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);   // Grizzly Bears 2/2
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), null);

        // Resolve only the attack trigger (before combat damage) to isolate its damage.
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage scales with the equipped creature's current power")
    void damageScalesWithPower() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        creature.setPowerModifier(3); // 2/2 -> 5 power
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), null);
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Reduces loyalty of the attacked planeswalker equal to the creature's power")
    void dealsPowerDamageToAttackedPlaneswalker() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 4 - 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No attack trigger fires when the creature is not equipped")
    void noTriggerWhenUnequipped() {
        harness.setLife(player2, 20);
        addReadyCreature(player1);
        addMageSlayer(player1); // on the battlefield but not attached

        declareAttackers(player1, List.of(0), null);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Mage Slayer"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addMageSlayer(Player player) {
        Permanent perm = new Permanent(new MageSlayer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
