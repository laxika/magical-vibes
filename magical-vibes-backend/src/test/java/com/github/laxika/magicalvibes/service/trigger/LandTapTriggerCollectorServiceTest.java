package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Manabarbs;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Overgrowth;
import com.github.laxika.magicalvibes.cards.v.VorinclexVoiceOfHunger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LandTapTriggerCollectorServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("DealDamageOnLandTapEffect")
    class DealDamageOnLandTap {

        @Test
        @DisplayName("Manabarbs deals 1 damage when a player taps a land")
        void manabarbsDealsDamageOnLandTap() {
            harness.addToBattlefield(player1, new Manabarbs());
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());

            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player2.getId(), forest.getId());

            int lifeAfter = gd.playerLifeTotals.get(player2.getId());
            assertThat(lifeAfter).isEqualTo(lifeBefore - 1);
        }

        @Test
        @DisplayName("Manabarbs also damages its controller when they tap a land")
        void manabarbsDamagesControllerToo() {
            harness.addToBattlefield(player1, new Manabarbs());
            harness.addToBattlefield(player1, new Mountain());
            Permanent mountain = gd.playerBattlefields.get(player1.getId()).get(1);
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player1.getId(), mountain.getId());

            int lifeAfter = gd.playerLifeTotals.get(player1.getId());
            assertThat(lifeAfter).isEqualTo(lifeBefore - 1);
        }
    }

    @Nested
    @DisplayName("AddManaOnEnchantedLandTapEffect")
    class AddManaOnEnchantedLandTap {

        @Test
        @DisplayName("Overgrowth adds GG when enchanted land is tapped")
        void overgrowthAddsGreenMana() {
            harness.addToBattlefield(player1, new Forest());
            Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();

            // Manually put Overgrowth on the battlefield attached to the forest
            Overgrowth overgrowth = new Overgrowth();
            harness.addToBattlefield(player1, overgrowth);
            Permanent overgrowthPerm = gd.playerBattlefields.get(player1.getId()).get(1);
            overgrowthPerm.setAttachedTo(forest.getId());

            ManaPool poolBefore = gd.playerManaPools.get(player1.getId());
            int greenBefore = poolBefore.get(ManaColor.GREEN);

            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player1.getId(), forest.getId());

            int greenAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
            assertThat(greenAfter).isEqualTo(greenBefore + 2);
        }

        @Test
        @DisplayName("Overgrowth does not trigger when a different land is tapped")
        void overgrowthDoesNotTriggerForDifferentLand() {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            Permanent forest1 = gd.playerBattlefields.get(player1.getId()).get(0);
            Permanent forest2 = gd.playerBattlefields.get(player1.getId()).get(1);

            Overgrowth overgrowth = new Overgrowth();
            harness.addToBattlefield(player1, overgrowth);
            Permanent overgrowthPerm = gd.playerBattlefields.get(player1.getId()).get(2);
            overgrowthPerm.setAttachedTo(forest1.getId());

            ManaPool poolBefore = gd.playerManaPools.get(player1.getId());
            int greenBefore = poolBefore.get(ManaColor.GREEN);

            // Tap forest2, NOT the enchanted forest1
            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player1.getId(), forest2.getId());

            int greenAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
            assertThat(greenAfter).isEqualTo(greenBefore);
        }
    }

    @Nested
    @DisplayName("OpponentTappedLandDoesntUntapEffect")
    class OpponentLandDoesntUntap {

        @Test
        @DisplayName("Vorinclex makes opponent's tapped land skip next untap")
        void vorinclexSkipsOpponentUntap() {
            harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            assertThat(forest.getSkipUntapCount()).isZero();

            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player2.getId(), forest.getId());

            assertThat(forest.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Vorinclex does not affect controller's own lands")
        void vorinclexDoesNotAffectOwnLands() {
            harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
            harness.addToBattlefield(player1, new Forest());
            // Vorinclex is at index 0, Forest is at index 1
            Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);

            harness.getTriggerCollectionService().checkLandTapTriggers(gd, player1.getId(), forest.getId());

            assertThat(forest.getSkipUntapCount()).isZero();
        }
    }
}
