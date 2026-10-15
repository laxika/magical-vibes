package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AnointWithAffliction;
import com.github.laxika.magicalvibes.cards.b.BranchblightStalker;
import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.cards.p.PredationSteward;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecrogenCommunion.class, PredationSteward.class, HexgoldSlash.class,
        BranchblightStalker.class, AnointWithAffliction.class})
class NecrogenCommunionTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasToxicTwoAndGivesTwoPoisonCountersOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TOXIC)).isTrue();

        creature.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void returnsEnchantedCreatureToBattlefieldUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);

        killCreature(creature);

        Permanent returned = findPermanent(player1, "Predation Steward");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void canEnchantOnlyACreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new PredationSteward());
        harness.setHand(player1, List.of(new NecrogenCommunion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void toxicAppliesDuringCombatDamageWithoutUsingTheStack() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);
        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedToxicAddsToPrintedToxicDuringCombatDamage() {
        Permanent creature = addCreatureReady(player1, new BranchblightStalker());
        castNecrogenCommunion(player1, creature);
        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoCommunionsGrantCumulativeToxicDuringCombatDamage() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);
        castNecrogenCommunion(player1, creature);
        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilingEnchantedCreatureDoesNotReturnIt() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Predation Steward");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
        harness.assertInGraveyard(player1, "Necrogen Communion");
    }

    @Test
    void returnedCreatureDoesNotRetainToxicOrReturnAfterAnotherDeath() {
        Permanent creature = addCreatureReady(player1, new PredationSteward());
        castNecrogenCommunion(player1, creature);

        killCreature(creature);

        Permanent returned = findPermanent(player1, "Predation Steward");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.TOXIC)).isFalse();
        harness.assertInGraveyard(player1, "Necrogen Communion");

        killCreature(returned);

        harness.assertNotOnBattlefield(player1, "Predation Steward");
        harness.assertInGraveyard(player1, "Predation Steward");
    }

    private void castNecrogenCommunion(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new NecrogenCommunion()));
        harness.addMana(controller, ManaColor.BLACK, 2);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();
    }

}
