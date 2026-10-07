package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritedSimulacrum.class, Forest.class, GrizzlyBears.class, WrathOfGod.class,
        DryadArbor.class, GrafdiggersCage.class})
class SpiritedSimulacrumTest extends BaseCardTest {

    @Test
    void entersWithASeekingLandTapped() {
        Forest land = new Forest();
        GrizzlyBears otherLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(otherLibraryCard, land));

        harness.enterBattlefieldAndReturn(player1, new SpiritedSimulacrum());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
    }

    @Test
    void seeksANonlandCardWhenItLeavesTheBattlefield() {
        Forest land = new Forest();
        GrizzlyBears soughtCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, soughtCard));
        harness.addToBattlefield(player1, new SpiritedSimulacrum());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SpiritedSimulacrum);
    }

    @Test
    void canBeCastForItsWarpCost() {
        harness.setHand(player1, List.of(new SpiritedSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent simulacrum = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpiritedSimulacrum)
                .findFirst()
                .orElseThrow();
        assertThat(simulacrum.isCastWithWarp()).isTrue();
    }

    @Test
    @CardUsed({SpiritedSimulacrum.class, DryadArbor.class, GrafdiggersCage.class})
    void soughtCreatureLandEntersFromHandDespiteGrafdiggersCage() {
        DryadArbor land = new DryadArbor();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player2, new GrafdiggersCage());

        harness.enterBattlefieldAndReturn(player1, new SpiritedSimulacrum());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
    }

    @Test
    void enteringWithoutALandLeavesTheLibraryUnchanged() {
        SpiritedSimulacrum nonland = new SpiritedSimulacrum();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new SpiritedSimulacrum());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void leavingWithoutANonlandLeavesTheLibraryUnchanged() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addToBattlefield(player1, new SpiritedSimulacrum());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void warpExileSeeksANonlandAndRecastingDoesNotExileAgain() {
        SpiritedSimulacrum card = new SpiritedSimulacrum();
        card.setOwnerId(player1.getId());
        Forest land = new Forest();
        SpiritedSimulacrum soughtCard = new SpiritedSimulacrum();
        harness.setLibrary(player1, List.of(land, soughtCard,
                new SpiritedSimulacrum(), new SpiritedSimulacrum()));
        harness.setLibrary(player2, List.of(new SpiritedSimulacrum(), new SpiritedSimulacrum()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
        assertThat(gd.stack).isNotEmpty();
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == card);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(sought -> sought instanceof SpiritedSimulacrum);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize - 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == card);
    }
}
