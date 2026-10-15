package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifetap.class, Forest.class, Island.class, Twiddle.class})
class LifetapTest extends BaseCardTest {

    // "Whenever a Forest an opponent controls becomes tapped, you gain 1 life."

    @Test
    @DisplayName("An opponent's Forest becoming tapped gains the controller 1 life")
    void opponentForestTapGainsLife() {
        harness.addToBattlefield(player1, new Lifetap());
        harness.addToBattlefield(player2, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each time an opponent's Forest becomes tapped, the controller gains 1 life")
    void eachOpponentForestTapTriggersSeparately() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        forest.untap();
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping your own Forest does not trigger (only opponents)")
    void ownForestTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifetap());
        harness.addToBattlefield(player1, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Forest land does not trigger")
    void opponentNonForestTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifetap());
        harness.addToBattlefield(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A spell tapping an opponent's Forest triggers Lifetap without producing mana")
    void forcedForestTapGainsLife() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Untapping an opponent's Forest does not trigger Lifetap")
    void untappingForestDoesNotGainLife() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Each Lifetap triggers independently for the same Forest tap")
    void multipleCopiesGainLifeSeparately() {
        harness.addToBattlefield(player1, new Lifetap());
        harness.addToBattlefield(player1, new Lifetap());
        harness.addToBattlefield(player2, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
