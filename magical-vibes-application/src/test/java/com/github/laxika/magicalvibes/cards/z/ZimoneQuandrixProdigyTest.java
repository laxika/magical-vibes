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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZimoneQuandrixProdigyTest extends BaseCardTest {

    @Nested
    @DisplayName("First ability")
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
    class SecondAbility {

        @Test
        @DisplayName("Draws one card with fewer than eight lands")
        void drawsOneCardWithFewerThanEightLands() {
            addReadyZimone(player1);
            Card forest = new Forest();
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).add(forest);
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
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(forest, island));
            harness.setHand(player1, List.of());
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        }
    }

    private Permanent addReadyZimone(Player player) {
        Permanent zimone = new Permanent(new ZimoneQuandrixProdigy());
        zimone.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(zimone);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return zimone;
    }
}
