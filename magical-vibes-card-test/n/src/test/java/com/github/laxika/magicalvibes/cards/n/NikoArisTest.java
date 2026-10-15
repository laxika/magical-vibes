package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NikoAris.class, Forest.class, GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class})
class NikoArisTest extends BaseCardTest {

    @Test
    void castWithXCreatesShardTokens() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(shards).hasSize(1);
        assertThat(shards.getFirst().getCard().getType()).isEqualTo(CardType.ENCHANTMENT);
    }

    @Test
    void shardSacrificesToScryAndDraw() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int shardIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Shard"));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, shardIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Shard");
    }

    @Test
    void plusOneReturnsTheCreatureAfterItDealsDamage() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void minusOneDealsTwoDamagePerCardDrawnToTappedCreature() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void zeroXCreatesNoShards() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Niko Aris");
        harness.assertNotOnBattlefield(player1, "Shard");
    }

    @Test
    void largerXCreatesThatManyShards() {
        harness.setHand(player1, List.of(new NikoAris()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castPlaneswalker(player1, 0, 3);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Shard")).isEqualTo(3);
    }

    @Test
    void lastMinusOneCreatesAShardEvenWhenNikoDies() {
        addReadyNiko(player1, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Niko Aris");
        assertThat(countPermanents(player1, "Shard")).isEqualTo(1);
    }

    @Test
    void plusOneCanChooseNoCreature() {
        Permanent niko = addReadyNiko(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(niko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Niko Aris");
    }

    @Test
    void plusOnePreventsBlocking() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1)))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneReturnsCreatureAfterNoncombatDamageEvenWithoutNiko() {
        Permanent niko = addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(niko);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Prodigal Pyromancer");
        harness.assertNotOnBattlefield(player1, "Prodigal Pyromancer");
    }

    @Test
    void damageAbilityDoesNothingIfTargetUntapsBeforeResolution() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        creature.untap();

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void damageAbilityCountsOnlyControllersDrawsAndUsesResolutionCount() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        gd.cardsDrawnThisTurn.put(player2.getId(), 4);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void shardScryCanBottomTopCardBeforeDrawing() {
        addReadyNiko(player1, 3);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Shard");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Forest");
    }


    @Test
    void enteringWithoutCastingCreatesNoShards() {
        harness.enterBattlefieldAndReturn(player1, new NikoAris());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Niko Aris");
        harness.assertNotOnBattlefield(player1, "Shard");
    }

    @Test
    void plusOneCannotTargetOpponentsCreature() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityCannotTargetUntappedCreature() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityDealsNoDamageWithoutDraws() {
        addReadyNiko(player1, 3);
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        gd.cardsDrawnThisTurn.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent addReadyNiko(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NikoAris());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
