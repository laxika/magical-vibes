package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceCelebration;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mindcrank;
import com.github.laxika.magicalvibes.cards.r.RelicPutrescence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MiscTriggerCollectorServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED")
    class AllySacrificed {

        @Test
        @DisplayName("Furnace Celebration queues may ability when ally permanent is sacrificed")
        void furnaceCelebrationQueuesOnSacrifice() {
            harness.addToBattlefield(player1, new FurnaceCelebration());

            harness.getTriggerCollectionService().checkAllyPermanentSacrificedTriggers(gd, player1.getId());

            // Furnace Celebration uses MayPayManaEffect — should be queued
            assertThat(gd.pendingMayAbilities).isNotEmpty();
        }

        @Test
        @DisplayName("No trigger when no ON_ALLY_PERMANENT_SACRIFICED effects on battlefield")
        void noTriggerWithNoEffects() {
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.getTriggerCollectionService().checkAllyPermanentSacrificedTriggers(gd, player1.getId());

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — GiveEnchantedPermanentControllerPoisonCountersEffect")
    class EnchantedPermanentTap {

        @Test
        @DisplayName("Relic Putrescence puts ability on stack when enchanted artifact is tapped")
        void relicPutrescenceTriggersOnTap() {
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            RelicPutrescence relic = new RelicPutrescence();
            harness.addToBattlefield(player1, relic);
            Permanent relicPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
            relicPerm.setAttachedTo(forest.getId());

            int stackBefore = gd.stack.size();
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, forest);

            assertThat(gd.stack).hasSize(stackBefore + 1);
            assertThat(gd.stack.getLast().getDescription()).contains("Relic Putrescence");
        }

        @Test
        @DisplayName("Does not trigger for unattached enchantments")
        void doesNotTriggerForUnattached() {
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            RelicPutrescence relic = new RelicPutrescence();
            harness.addToBattlefield(player1, relic);
            // NOT attached to anything

            int stackBefore = gd.stack.size();
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, forest);

            assertThat(gd.stack).hasSize(stackBefore);
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_LOSES_LIFE — MillOpponentOnLifeLossEffect")
    class LifeLossMill {

        @Test
        @DisplayName("Mindcrank mills opponent for amount of life lost")
        void mindcrankMillsOnLifeLoss() {
            harness.addToBattlefield(player1, new Mindcrank());

            // Give opponent some library cards to mill
            int libraryBefore = gd.playerDecks.get(player2.getId()).size();
            int lifeLost = 3;

            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), lifeLost);

            int libraryAfter = gd.playerDecks.get(player2.getId()).size();
            int graveyardAfter = gd.playerGraveyards.get(player2.getId()).size();

            // Should have milled 3 cards (or fewer if library was smaller)
            int milled = libraryBefore - libraryAfter;
            assertThat(milled).isEqualTo(Math.min(lifeLost, libraryBefore));
        }

        @Test
        @DisplayName("Mindcrank does not trigger for 0 life loss")
        void mindcrankDoesNotTriggerForZeroLifeLoss() {
            harness.addToBattlefield(player1, new Mindcrank());
            int libraryBefore = gd.playerDecks.get(player2.getId()).size();

            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 0);

            int libraryAfter = gd.playerDecks.get(player2.getId()).size();
            assertThat(libraryAfter).isEqualTo(libraryBefore);
        }

        @Test
        @DisplayName("Mindcrank does not trigger when controller loses life")
        void mindcrankDoesNotTriggerOnControllerLifeLoss() {
            harness.addToBattlefield(player1, new Mindcrank());
            int libraryBefore = gd.playerDecks.get(player1.getId()).size();

            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 3);

            int libraryAfter = gd.playerDecks.get(player1.getId()).size();
            assertThat(libraryAfter).isEqualTo(libraryBefore);
        }
    }
}
