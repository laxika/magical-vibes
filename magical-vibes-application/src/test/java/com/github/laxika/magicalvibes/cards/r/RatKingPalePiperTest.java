package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RatKingPalePiper.class, GrizzlyBears.class})
class RatKingPalePiperTest extends BaseCardTest {

    @Test
    void createsRatWhenAnotherNontokenCreatureLeavesBattlefield() {
        addCreatureReady(player1, new RatKingPalePiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(bears);

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void createsRatWhenRatKingLeavesBattlefield() {
        Permanent ratKing = addCreatureReady(player1, new RatKingPalePiper());

        removeFromBattlefield(ratKing);

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void doesNotTriggerForTokenCreature() {
        addCreatureReady(player1, new RatKingPalePiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        removeFromBattlefield(bears);
        Permanent rat = findPermanent(player1, "Rat");

        removeFromBattlefield(rat);

        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void sacrificesTokenToDrawCard() {
        addCreatureReady(player1, new RatKingPalePiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        removeFromBattlefield(bears);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void cannotActivateWithoutToken() {
        addCreatureReady(player1, new RatKingPalePiper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }
}
