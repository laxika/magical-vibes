package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NacatlWarPride.class, GrizzlyBears.class})
class NacatlWarPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Nacatl War-Pride must be blocked by exactly one creature when possible")
    void mustBeBlockedByExactlyOneCreature() {
        Permanent warPride = addReadyCreature(player1, new NacatlWarPride());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
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
        addReadyCreature(player1, new NacatlWarPride());
        Permanent defenderOne = addReadyCreature(player2, new GrizzlyBears());
        Permanent defenderTwo = addReadyCreature(player2, new GrizzlyBears());
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
            assertThat(copy.isAttackedThisTurn()).isTrue();
            assertThat(copy.getCard().getPower()).isEqualTo(3);
            assertThat(copy.getCard().getToughness()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Copies are exiled at the beginning of the next end step")
    void copiesAreExiledAtNextEndStep() {
        addReadyCreature(player1, new NacatlWarPride());
        Permanent defender = addReadyCreature(player2, new GrizzlyBears());
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

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
