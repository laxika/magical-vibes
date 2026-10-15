package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QalSismaBehemoth.class, SummitProwler.class, BraveTheSands.class})
class QalSismaBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without paying {2}")
    void cannotAttackWithoutPayment() {
        Permanent behemoth = addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(behemoth.isAttacking()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacks when its controller pays {2}")
    void attacksWhenPaid() {
        addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.creaturesAttackedCountThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot block without paying {2}")
    void cannotBlockWithoutPayment() {
        addCreatureReady(player1, new SummitProwler());
        Permanent behemoth = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.RED, 1);
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(behemoth.isBlocking()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocks when its controller pays {2}")
    void blocksWhenPaid() {
        addCreatureReady(player1, new SummitProwler());
        Permanent behemoth = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.RED, 2);
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(behemoth.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each attacking Behemoth requires its own payment")
    void multipleAttackersRequireSeparatePayments() {
        Permanent first = addCreatureReady(player1, new QalSismaBehemoth());
        Permanent second = addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Generic attack costs can be paid with different mana colors")
    void multipleAttackersCanPayWithMixedColors() {
        addCreatureReady(player1, new QalSismaBehemoth());
        addCreatureReady(player1, new QalSismaBehemoth());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.creaturesAttackedCountThisTurn.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each blocking Behemoth requires its own payment")
    void multipleBlockersRequireSeparatePayments() {
        addCreatureReady(player1, new SummitProwler());
        Permanent first = addCreatureReady(player2, new QalSismaBehemoth());
        Permanent second = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.RED, 3);
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isBlocking()).isFalse();
        assertThat(second.isBlocking()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Generic block costs can be paid with different mana colors")
    void multipleBlockersCanPayWithMixedColors() {
        addCreatureReady(player1, new SummitProwler());
        Permanent first = addCreatureReady(player2, new QalSismaBehemoth());
        Permanent second = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("One Behemoth pays only {2} when blocking two attackers")
    void paysOnceWhenBlockingMultipleAttackers() {
        addCreatureReady(player1, new SummitProwler());
        addCreatureReady(player1, new SummitProwler());
        Permanent behemoth = addCreatureReady(player2, new QalSismaBehemoth());
        harness.addToBattlefield(player2, new BraveTheSands());
        harness.addMana(player2, ManaColor.RED, 2);
        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(behemoth.isBlocking()).isTrue();
        assertThat(behemoth.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
