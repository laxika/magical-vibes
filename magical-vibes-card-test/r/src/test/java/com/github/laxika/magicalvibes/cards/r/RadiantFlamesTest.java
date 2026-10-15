package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantFlames.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class, Twincast.class})
class RadiantFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Snapshots the number of distinct colored mana spent for Converge")
    void snapshotsDistinctColorsSpent() {
        harness.setHand(player1, List.of(new RadiantFlames()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals Converge damage to each creature but not to players")
    void damagesEachCreatureOnly() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RadiantFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Colorless mana does not increase Converge")
    void colorlessManaDoesNotIncreaseConverge() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiantFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Three colors deal three damage even when more colors are available")
    void threeColorsKillThreeToughnessCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RadiantFlames()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Repeated red mana counts as one color and later mana does not change damage")
    void repeatedColorAndLaterManaDoNotIncreaseDamage() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiantFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell copy deals no damage because no mana was spent to cast it")
    void copiedSpellDealsNoDamage() {
        RadiantFlames flames = new RadiantFlames();
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(flames));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, flames.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }
}
