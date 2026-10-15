package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(EdgewallPack.class)
class EdgewallPackTest extends BaseCardTest {

    @Test
    void entersAndCreatesNonblockingRatToken() {
        harness.setHand(player1, List.of(new EdgewallPack()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(rats.getFirst().getCard().isToken()).isTrue();
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void ratIsCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new EdgewallPack()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Rat")).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Edgewall Pack");
        assertThat(countPermanents(player1, "Rat")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(rat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(rat.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
        assertThat(rat.isTapped()).isFalse();
        assertThat(countPermanents(player2, "Rat")).isZero();
    }

    @Test
    void opponentCreatesRatUnderTheirControl() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EdgewallPack()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Rat")).isEqualTo(1);
        assertThat(countPermanents(player1, "Rat")).isZero();
        assertThat(bls.canBlock(gd, findPermanent(player2, "Rat"))).isFalse();
    }

    @Test
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new EdgewallPack());
        Permanent firstBlocker = addCreatureReady(player2, new EdgewallPack());
        Permanent secondBlocker = addCreatureReady(player2, new EdgewallPack());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
