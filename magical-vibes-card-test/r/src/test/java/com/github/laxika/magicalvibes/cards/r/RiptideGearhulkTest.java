package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MomentumBreaker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideGearhulk.class, GrizzlyBears.class, Plains.class, MomentumBreaker.class})
class RiptideGearhulkTest extends BaseCardTest {

    @Test
    void putsTargetOnBottomWhenLibraryHasFewerThanTwoCards() {
        Plains top = new Plains();
        harness.setLibrary(player2, List.of(top));
        RiptideGearhulk card = new RiptideGearhulk();
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);

        castRiptideGearhulk(List.of(target.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, card);
        harness.assertNotOnBattlefield(player2, "Riptide Gearhulk");
    }

    @Test
    void cannotChooseTwoPermanentsControlledByTheSameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RiptideGearhulk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RiptideGearhulk());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void prowessResolvesBeforeTheNoncreatureSpell() {
        Permanent gearhulk = harness.addToBattlefieldAndReturn(player1, new RiptideGearhulk());
        harness.castFromHand(player1, new MomentumBreaker(), "{1}{B}");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gearhulk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gearhulk)).isEqualTo(6);
        harness.assertNotOnBattlefield(player1, "Momentum Breaker");
    }

    @Test
    void unblockedDoubleStrikeDealsDamageInBothSteps() {
        addCreatureReady(player1, new RiptideGearhulk());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Puts a targeted nonland permanent third from the top of its owner's library")
    void putsTargetThirdFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRiptideGearhulk(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        castRiptideGearhulk(List.of());

        harness.assertOnBattlefield(player1, "Riptide Gearhulk");
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by its controller")
    void cannotTargetLandOrOwnPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRiptideGearhulk(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new RiptideGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
