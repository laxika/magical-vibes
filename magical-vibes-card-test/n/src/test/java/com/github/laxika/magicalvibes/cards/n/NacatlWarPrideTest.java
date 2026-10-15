package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GoblinWarDrums;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NacatlWarPride.class, BladeOfTheSixthPride.class, ChandraNalaar.class,
        ZoeticCavern.class, GoblinWarDrums.class})
class NacatlWarPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Nacatl War-Pride must be blocked by exactly one creature when possible")
    void mustBeBlockedByExactlyOneCreature() {
        Permanent warPride = addCreatureReady(player1, new NacatlWarPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());
        warPride.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Attacking creates one tapped copy per defending creature")
    void attackingCreatesTappedCopiesForDefendingCreatures() {
        addCreatureReady(player1, new NacatlWarPride());
        Permanent defenderOne = addCreatureReady(player2, new BladeOfTheSixthPride());
        Permanent defenderTwo = addCreatureReady(player2, new BladeOfTheSixthPride());
        defenderOne.tap();
        defenderTwo.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
            assertThat(copy.isAttackedThisTurn()).isFalse();
            assertThat(copy.getCard().getPower()).isEqualTo(3);
            assertThat(copy.getCard().getToughness()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("The attack trigger counts creatures controlled when it resolves")
    void attackTriggerCountsDefendingCreaturesAtResolution() {
        addCreatureReady(player1, new NacatlWarPride());
        Permanent defenderOne = addCreatureReady(player2, new BladeOfTheSixthPride());
        defenderOne.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        Permanent defenderTwo = addCreatureReady(player2, new BladeOfTheSixthPride());
        defenderTwo.tap();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("The attack trigger still creates copies if Nacatl War-Pride leaves before resolution")
    void attackTriggerCreatesCopiesAfterSourceLeaves() {
        Permanent warPride = addCreatureReady(player1, new NacatlWarPride());
        Permanent defender = addCreatureReady(player2, new BladeOfTheSixthPride());
        defender.tap();

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(warPride);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @CardUsed({ChandraNalaar.class})
    @DisplayName("The attack trigger still counts the defending player if its planeswalker target leaves")
    void attackTriggerRetainsDefendingPlayerWhenAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new NacatlWarPride());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        addCreatureReady(player2, new BladeOfTheSixthPride());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        List<Permanent> copies = findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
    }

    @Test
    @DisplayName("Copies are exiled at the beginning of the next end step")
    void copiesAreExiledAtNextEndStep() {
        addCreatureReady(player1, new NacatlWarPride());
        Permanent defender = addCreatureReady(player2, new BladeOfTheSixthPride());
        defender.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Token copies retain the exactly-one-blocker restriction")
    void tokenCopiesRetainExactlyOneBlockerRestriction() {
        addCreatureReady(player1, new NacatlWarPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        addCreatureReady(player2, new BladeOfTheSixthPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, tokenIndex),
                new BlockerAssignment(2, tokenIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @CardUsed({ZoeticCavern.class})
    @DisplayName("Only creatures controlled by the defending player are counted")
    void onlyCreaturesControlledByDefendingPlayerAreCounted() {
        addCreatureReady(player1, new NacatlWarPride());
        addCreatureReady(player1, new BladeOfTheSixthPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());
        harness.addToBattlefield(player2, new ZoeticCavern());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("With no defending creatures, the attack creates no copies and needs no blocker")
    void noDefendingCreaturesMeansNoCopiesOrBlockerRequirement() {
        addCreatureReady(player1, new NacatlWarPride());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @CardUsed({GoblinWarDrums.class})
    @DisplayName("A War-Pride with menace may be blocked by two creatures")
    void menaceAllowsMultipleBlockersWhenExactlyOneIsImpossible() {
        Permanent warPride = addCreatureReady(player1, new NacatlWarPride());
        harness.addToBattlefield(player1, new GoblinWarDrums());
        addCreatureReady(player2, new BladeOfTheSixthPride());
        addCreatureReady(player2, new BladeOfTheSixthPride());
        warPride.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
    }

    @Test
    @DisplayName("End-step exile uses a delayed trigger that can be responded to")
    void copiesRemainUntilDelayedExileTriggerResolves() {
        addCreatureReady(player1, new NacatlWarPride());
        addCreatureReady(player2, new BladeOfTheSixthPride()).tap();
        addCreatureReady(player2, new BladeOfTheSixthPride()).tap();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Nacatl War-Pride").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }
}
