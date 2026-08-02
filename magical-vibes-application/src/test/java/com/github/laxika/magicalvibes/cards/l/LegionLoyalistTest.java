package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegionLoyalistTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants first strike and trample to creatures you control")
    void battalionGrantsFirstStrikeAndTrample() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

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
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature tokens can't block your creatures after battalion triggers")
    void creatureTokensCannotBlock() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, createTokenCreature("Soldier Token", 1, 1));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by nontoken creatures");
    }

    @Test
    @DisplayName("Nontoken creatures can still block after battalion triggers")
    void nontokenCreaturesCanStillBlock() {
        addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Battalion grants wear off at end of turn")
    void grantsWearOff() {
        Permanent loyalist = addCreatureReady(player1, new LegionLoyalist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(loyalist.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(loyalist.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(loyalist.getBlockRestrictionsUntilEndOfTurn()).isEmpty();
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
