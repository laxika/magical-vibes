package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OrderOfTheSacredBell;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideousLaughter.class, HumbleBudoka.class, OrderOfTheSacredBell.class, ReachThroughMists.class, YamabushisFlame.class})
class HideousLaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2, killing X/2s on both sides")
    void shrinksAllCreatures() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());
        Permanent monk = addCreatureReady(player2, new OrderOfTheSacredBell());
        harness.setHand(player1, List.of(new HideousLaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Humble Budoka");
        harness.assertInGraveyard(player2, "Humble Budoka");
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent monk = addCreatureReady(player2, new OrderOfTheSacredBell());
        harness.setHand(player1, List.of(new HideousLaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        addCreatureReady(player2, new HumbleBudoka());
        ReachThroughMists arcaneSpell = new ReachThroughMists();
        HideousLaughter hideousLaughter = new HideousLaughter();
        HumbleBudoka drawnCard = new HumbleBudoka();
        harness.setHand(player1, List.of(arcaneSpell, hideousLaughter));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, null, List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Humble Budoka");
        harness.assertInGraveyard(player1, "Reach Through Mists");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hideousLaughter, drawnCard);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void rejectsNonArcaneHost() {
        YamabushisFlame flame = new YamabushisFlame();
        HideousLaughter hideousLaughter = new HideousLaughter();
        harness.setHand(player1, List.of(flame, hideousLaughter));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(flame, hideousLaughter);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectLaterCreatures() {
        harness.setHand(player1, List.of(new HideousLaughter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        Permanent budoka = addCreatureReady(player2, new HumbleBudoka());

        harness.assertOnBattlefield(player2, "Humble Budoka");
        assertThat(gqs.getEffectivePower(gd, budoka)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, budoka)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two different copies can be spliced onto the same spell")
    void splicesTwoCopies() {
        addCreatureReady(player2, new OrderOfTheSacredBell());
        HideousLaughter first = new HideousLaughter();
        HideousLaughter second = new HideousLaughter();
        HumbleBudoka drawnCard = new HumbleBudoka();
        harness.setHand(player1, List.of(new ReachThroughMists(), first, second));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castWithSplice(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Order of the Sacred Bell");
        harness.assertInGraveyard(player1, "Reach Through Mists");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, drawnCard);
    }

    @Test
    @DisplayName("Splice requires its full cost in addition to the host spell's cost")
    void rejectsInsufficientSpliceMana() {
        ReachThroughMists host = new ReachThroughMists();
        HideousLaughter laughter = new HideousLaughter();
        harness.setHand(player1, List.of(host, laughter));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(host, laughter);
        assertThat(gd.stack).isEmpty();
    }
}
