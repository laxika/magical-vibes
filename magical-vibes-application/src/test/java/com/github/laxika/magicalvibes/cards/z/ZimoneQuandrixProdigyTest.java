package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneQuandrixProdigy.class, Forest.class, Island.class, GrizzlyBears.class})
class ZimoneQuandrixProdigyTest extends BaseCardTest {

    @Nested
    @DisplayName("First ability")
    @CardUsed({ZimoneQuandrixProdigy.class, Forest.class, Island.class, GrizzlyBears.class})
    class FirstAbility {

        @Test
        @DisplayName("May put one land from hand onto the battlefield tapped")
        void putsOneLandFromHandTapped() {
            addReadyZimone(player1);
            Card forest = new Forest();
            Card island = new Island();
            Card bears = new GrizzlyBears();
            harness.setHand(player1, List.of(forest, island, bears));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(island, bears);
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard() == forest)
                    .findFirst()
                    .orElseThrow()
                    .isTapped()).isTrue();
        }

        @Test
        @DisplayName("Declining puts no land onto the battlefield")
        void decliningPutsNoLand() {
            addReadyZimone(player1);
            Card forest = new Forest();
            harness.setHand(player1, List.of(forest));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .noneMatch(permanent -> permanent.getCard() == forest)).isTrue();
        }
    }

    @Nested
    @DisplayName("Second ability")
    @CardUsed({ZimoneQuandrixProdigy.class, Forest.class, Island.class})
    class SecondAbility {

        @Test
        @DisplayName("Draws one card with fewer than eight lands")
        void drawsOneCardWithFewerThanEightLands() {
            addReadyZimone(player1);
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(forest));
            harness.setHand(player1, List.of());
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        }

        @Test
        @DisplayName("Draws two cards with eight lands")
        void drawsTwoCardsWithEightLands() {
            addReadyZimone(player1);
            for (int i = 0; i < 8; i++) {
                harness.addToBattlefield(player1, new Forest());
            }
            Card forest = new Forest();
            Card island = new Island();
            harness.setLibrary(player1, List.of(forest, island));
            harness.setHand(player1, List.of());
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        }
    }

    @Test
    @DisplayName("Seven lands plus opposing lands still draws only one card")
    void opponentsLandsDoNotCount() {
        addReadyZimone(player1);
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("More than eight lands draws exactly two cards")
    void drawsExactlyTwoWithMoreThanEightLands() {
        addReadyZimone(player1);
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Card first = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("An eighth land arriving before resolution upgrades the draw")
    void countsLandsAtResolution() {
        addReadyZimone(player1);
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("The activated draw resolves after Zimone leaves the battlefield")
    void drawResolvesWithoutSource() {
        Permanent zimone = addReadyZimone(player1);
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(zimone);
        gd.playerGraveyards.get(player1.getId()).add(zimone.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Putting a land onto the battlefield works after the turn's land play")
    void putsLandAfterNormalLandPlay() {
        Permanent zimone = addReadyZimone(player1);
        Card island = new Island();
        Card forest = new Forest();
        harness.setHand(player1, List.of(island, forest));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(zimone.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        harness.assertOnBattlefield(player1, "Island");
    }
    private Permanent addReadyZimone(Player player) {
        Permanent zimone = harness.addToBattlefieldAndReturn(player, new ZimoneQuandrixProdigy());
        zimone.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return zimone;
    }
}
