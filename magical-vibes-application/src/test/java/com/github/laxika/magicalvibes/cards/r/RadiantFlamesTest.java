package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantFlames.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
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
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
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
}
