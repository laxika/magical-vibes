package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.s.SorinLordOfInnistrad;
import com.github.laxika.magicalvibes.cards.v.VaultOfTheArchangel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hellrider.class, DawntreaderElk.class, SorinLordOfInnistrad.class, VaultOfTheArchangel.class})
class HellriderTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers once for each creature you control that attacks")
    void triggersForEachAttackingCreature() {
        harness.setLife(player2, 20);
        Permanent hellrider = addCreatureReady(player1, new Hellrider());
        Permanent bears = addCreatureReady(player1, new DawntreaderElk());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Hellrider");
            assertThat(entry.getSourcePermanentId()).isEqualTo(gd.playerBattlefields.get(player1.getId()).getFirst().getId());
            // The triggering attacker is recorded as a non-targeting reference
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getAttackedTargetId()).isEqualTo(player2.getId());
        });
        // One trigger per attacking creature, each referencing its own attacker
        assertThat(gd.stack).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(hellrider.getId(), bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Does not trigger for opponent's attacking creatures")
    void doesNotTriggerForOpponentCreatures() {
        addCreatureReady(player1, new Hellrider());
        addCreatureReady(player2, new DawntreaderElk());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Hellrider")))
                .isEmpty();
    }

    @Test
    @DisplayName("Damages the planeswalker an attacking creature is attacking")
    void damagesAttackedPlaneswalker() {
        addCreatureReady(player1, new Hellrider());
        Permanent bears = addCreatureReady(player1, new DawntreaderElk());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        // The triggering attacker rides along as a non-targeting reference
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.stack.getFirst().getAttackedTargetId()).isEqualTo(planeswalker.getId());

        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A summoning-sick Hellrider can attack and trigger its own ability")
    void hasteAllowsImmediateAttack() {
        harness.addToBattlefield(player1, new Hellrider());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Multiple Hellriders each trigger even when neither attacks")
    void eachHellriderTriggersWhileNotAttacking() {
        addCreatureReady(player1, new Hellrider());
        addCreatureReady(player1, new Hellrider());
        addCreatureReady(player1, new DawntreaderElk());

        declareAttackers(List.of(2));

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removing Hellrider after its trigger is stacked does not stop its damage")
    void triggerResolvesAfterHellriderLeaves() {
        Permanent hellrider = addCreatureReady(player1, new Hellrider());
        addCreatureReady(player1, new DawntreaderElk());
        declareAttackers(List.of(1));

        gd.playerBattlefields.get(player1.getId()).remove(hellrider);
        gd.playerGraveyards.get(player1.getId()).add(hellrider.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Removing the attacker after its trigger is stacked does not stop Hellrider's damage")
    void triggerResolvesAfterAttackerLeaves() {
        addCreatureReady(player1, new Hellrider());
        Permanent elk = addCreatureReady(player1, new DawntreaderElk());
        declareAttackers(List.of(1));

        gd.playerBattlefields.get(player1.getId()).remove(elk);
        gd.playerGraveyards.get(player1.getId()).add(elk.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking a departed planeswalker does not redirect trigger damage to its controller")
    void departedPlaneswalkerDoesNotRedirectDamage() {
        addCreatureReady(player1, new Hellrider());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Hellrider's noncombat damage to a planeswalker gains life when Hellrider has lifelink")
    void planeswalkerTriggerDamageAppliesLifelink() {
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        addCreatureReady(player1, new Hellrider());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SorinLordOfInnistrad());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
