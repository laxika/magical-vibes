package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.e.EternalWitness;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Polygoyf.class, CommandTower.class, EternalWitness.class, SolRing.class, BeastWithin.class})
class PolygoyfTest extends BaseCardTest {

    @Test
    @DisplayName("has power equal to card types in all graveyards and toughness one higher")
    void powerAndToughnessCountCardTypesInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new CommandTower(), new BeastWithin()));
        harness.setGraveyard(player2, List.of(new SolRing(), new EternalWitness()));

        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        assertThat(gqs.getEffectivePower(gd, polygoyf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(5);
    }

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        Player player3 = addThirdPlayer();
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Polygoyf").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(polygoyf);
    }

    @Test
    void emptyGraveyardsGiveZeroPowerAndOneToughness() {
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        assertThat(gqs.getEffectivePower(gd, polygoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(1);
    }

    @Test
    void countsDistinctTypesAndUpdatesWhenGraveyardsChange() {
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());
        harness.setGraveyard(player1, List.of(new CommandTower(), new CommandTower()));
        harness.setGraveyard(player2, List.of(new CommandTower()));

        assertThat(gqs.getEffectivePower(gd, polygoyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new EternalWitness(), new SolRing()));
        assertThat(gqs.getEffectivePower(gd, polygoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, polygoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(1);
    }

    @Test
    void countsTypesInThirdPlayersGraveyard() {
        Player player3 = addThirdPlayer();
        harness.setGraveyard(player3, List.of(new SolRing(), new BeastWithin()));
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        assertThat(gqs.getEffectivePower(gd, polygoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, polygoyf)).isEqualTo(3);
    }

    @Test
    void myriadCanBeDeclined() {
        addThirdPlayer();
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Polygoyf")).containsExactly(polygoyf);
    }

    @Test
    void myriadCreatesNoCopiesInTwoPlayerGame() {
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Polygoyf")).containsExactly(polygoyf);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void myriadCopyRetainsDynamicGraveyardAbility() {
        addThirdPlayer();
        addCreatureReady(player1, new Polygoyf());
        harness.setGraveyard(player1, List.of(new CommandTower()));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Polygoyf").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new SolRing(), new BeastWithin()));
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }

    private Player addThirdPlayer() {
        return addOpponent("Charlie");
    }

    @Test
    void myriadExilesAllCopiesWithOneDelayedTrigger() {
        addThirdPlayer();
        addOpponent("Dana");
        Permanent polygoyf = addCreatureReady(player1, new Polygoyf());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        assertThat(findPermanents(player1, "Polygoyf")).hasSize(3);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> harness.passBothPriorities());
        assertThat(findPermanents(player1, "Polygoyf")).containsExactly(polygoyf);
    }

    private Player addOpponent(String name) {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, name);
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add(name);
        gd.playerIdToName.put(thirdPlayerId, name);
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-" + thirdPlayerId), thirdPlayerId, name);
        return player3;
    }
}
