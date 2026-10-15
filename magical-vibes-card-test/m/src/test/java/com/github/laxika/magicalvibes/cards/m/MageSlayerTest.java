package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BehemothSledge;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MageSlayer.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, BehemothSledge.class})
class MageSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature deals damage equal to its power to the attacked player")
    void dealsPowerDamageToAttackedPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());   // Grizzly Bears 2/2
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));

        // Resolve only the attack trigger (before combat damage) to isolate its damage.
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage scales with the equipped creature's current power")
    void damageScalesWithPower() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setPowerModifier(3); // 2/2 -> 5 power
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Reduces loyalty of the attacked planeswalker equal to the creature's power")
    void dealsPowerDamageToAttackedPlaneswalker() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 4 - 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No attack trigger fires when the creature is not equipped")
    void noTriggerWhenUnequipped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addMageSlayer(player1); // on the battlefield but not attached

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Mage Slayer"));
    }

    @Test
    void equipAttachesToControlledCreature() {
        Permanent slayer = addMageSlayer(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(slayer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void triggerStillDealsDamageAfterEquipmentLeaves() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0));

        gd.playerBattlefields.get(player1.getId()).remove(slayer);
        gd.playerGraveyards.get(player1.getId()).add(slayer.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void triggerUsesOriginalAttackerAfterEquipmentMoves() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0));

        slayer.setAttachedTo(other.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void usesPowerAtResolution() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0));

        creature.setPowerModifier(3);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void planeswalkerDamageAppliesCreaturesLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addMageSlayer(player1);
        slayer.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new BehemothSledge());
        findPermanent(player1, "Behemoth Sledge").setAttachedTo(creature.getId());
        Permanent planeswalker = addPlaneswalker(player2, 5);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addMageSlayer(Player player) {
        harness.addToBattlefield(player, new MageSlayer());
        return findPermanent(player, "Mage Slayer");
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        harness.addToBattlefield(player, new NicolBolasPlaneswalker());
        Permanent permanent = findPermanent(player, "Nicol Bolas, Planeswalker");
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
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
