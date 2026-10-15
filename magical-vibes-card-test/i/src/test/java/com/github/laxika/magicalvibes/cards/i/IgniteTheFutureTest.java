package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HoodedHydra;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgniteTheFuture.class, Forest.class, SakuraTribeElder.class, HoodedHydra.class})
class IgniteTheFutureTest extends BaseCardTest {

    @Test
    void exilesTopThreeCardsWithNormalPlayPermission() {
        Card first = new SakuraTribeElder();
        Card second = new Forest();
        Card third = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
    }

    @Test
    void flashbackAllowsPlayingExiledCardsWithoutPayingManaCosts() {
        Card first = new SakuraTribeElder();
        Card second = new Forest();
        Card third = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(first.getId(), second.getId(), third.getId());
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
        harness.assertNotInGraveyard(player1, "Ignite the Future");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof IgniteTheFuture);
    }

    @Test
    void canPlayExiledLandButCannotExceedLandLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void freePlayStillRequiresCreatureTimingAndControllersPermission() {
        Card creature = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(creature));
        harness.setGraveyard(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
    }

    @Test
    void permissionLastsThroughNextTurnThenExpiresLeavingUnusedCardsInExile() {
        Card creature = new SakuraTribeElder();
        Card unused = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(creature, unused, new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, unused.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(unused);
    }

    @Test
    void emptyLibraryDoesNotPreventNormalResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ignite the Future");
    }

    @Test
    void castingWithoutPayingManaCostCannotChooseNonzeroX() {
        Card hydra = new HoodedHydra();
        harness.setLibrary(player1, List.of(hydra));
        harness.setGraveyard(player1, List.of(new IgniteTheFuture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, hydra.getId(), 5, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hydra);
    }
}
