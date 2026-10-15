package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.ForbiddingWatchtower;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurstOfEnergy.class, GrizzlyBears.class, ForbiddingWatchtower.class})
class BurstOfEnergyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target permanent")
    void untapsTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps only the targeted noncreature permanent")
    void untapsOnlyTargetedNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());
        target.tap();
        other.tap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already untapped permanent you control")
    void canTargetUntappedPermanentYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ForbiddingWatchtower());
        target.untap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Burst of Energy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not untap another permanent when its target leaves the battlefield")
    void doesNotUntapAnotherPermanentWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());
        target.tap();
        other.tap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Burst of Energy");
        assertThat(gd.stack).isEmpty();
    }
}
