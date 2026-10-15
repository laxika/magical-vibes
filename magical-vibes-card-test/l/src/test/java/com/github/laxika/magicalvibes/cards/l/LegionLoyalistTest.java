package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.s.SimicManipulator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionLoyalist.class, GreensideWatcher.class, SimicManipulator.class})
class LegionLoyalistTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants first strike and trample to creatures you control")
    void battalionGrantsFirstStrikeAndTrample() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        Permanent otherAttacker = addCreatureReady(player1, new GreensideWatcher());
        Permanent opposingCreature = addCreatureReady(player2, new GreensideWatcher());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(otherAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(opposingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature tokens can't block your creatures after battalion triggers")
    void creatureTokensCannotBlock() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        harness.addToBattlefield(player2, createTokenCreature("Soldier Token", 1, 1));

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by nontoken creatures");
    }

    @Test
    @DisplayName("Nontoken creatures can still block after battalion triggers")
    void nontokenCreaturesCanStillBlock() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player2, new GreensideWatcher());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Battalion grants wear off at end of turn")
    void grantsWearOff() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.addToBattlefield(player2, createTokenCreature("Soldier Token", 1, 1));
        loyalist.untap();
        loyalist.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    void nonattackingCreaturesAlsoGainKeywords() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        Permanent nonattacker = addCreatureReady(player1, new GreensideWatcher());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(nonattacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(nonattacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void loyalistMustAttackForBattalionToTrigger() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void battalionResolvesAfterLoyalistLeavesBattlefield() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, loyalist));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void laterCreaturesGetBlockingRestrictionButNotKeywords() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        harness.addToBattlefield(player2, createTokenCreature("Soldier Token", 1, 1));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        Permanent lateCreature = addCreatureReady(player1, new GreensideWatcher());

        assertThat(lateCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(lateCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        lateCreature.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 3))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by nontoken creatures");
    }

    @Test
    void stolenLoyalistDoesNotReceiveOriginalControllersKeywordGrant() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        Permanent attacker = addCreatureReady(player1, new GreensideWatcher());
        addCreatureReady(player1, new GreensideWatcher());
        Permanent manipulator = addCreatureReady(player2, new SimicManipulator());
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        harness.activateAbility(player2, 0, 1, loyalist.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(loyalist);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
