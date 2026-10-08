package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowhornRider.class, WetlandSambar.class})
class SnowhornRiderTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SnowhornRider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent rider = findPermanent(player1, "Snowhorn Rider");
        assertThat(rider.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);
        harness.turnFaceUp(player1, riderIndex);
        harness.passBothPriorities();

        assertThat(rider.isFaceDown()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE", "RED"})
    void morphRequiresEachColoredManaAndDoesNotUseTheStack(ManaColor missing) {
        harness.setHand(player1, List.of(new SnowhornRider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent rider = findPermanent(player1, "Snowhorn Rider");
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            if (color != missing) {
                harness.addMana(player1, color, 1);
            }
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(rider);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, index))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rider.isFaceDown()).isTrue();

        harness.addMana(player1, missing, 1);
        harness.turnFaceUp(player1, index);

        assertThat(rider.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceUpRiderTramplesOverBlocker() {
        Permanent rider = addCreatureReady(player1, new SnowhornRider());
        Permanent blocker = addCreatureReady(player2, new WetlandSambar());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rider);
    }

    @Test
    void faceDownRiderHasNoTrampleAndTradesWithTwoPowerBlocker() {
        harness.setHand(player1, List.of(new SnowhornRider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent rider = findPermanent(player1, "Snowhorn Rider");
        rider.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new WetlandSambar());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rider);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
