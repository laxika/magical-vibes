package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremor.class, FugitiveWizard.class, SuntailHawk.class, GrizzlyBears.class})
class TremorTest extends BaseCardTest {

    @Test
    @DisplayName("Kills ground creatures on both sides")
    void killsGroundCreatures() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals exactly 1 damage to a larger ground creature")
    void dealsOneDamageToLargerGroundCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyard() {
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tremor");
    }
}
