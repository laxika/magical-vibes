package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonNecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new KeldonNecropolis());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a creature deals 2 damage to a player")
    void sacrificesCreatureToDamagePlayer() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Keldon Necropolis");
    }

    @Test
    @DisplayName("Sacrificing a creature deals 2 damage to a creature")
    void sacrificesCreatureToDamageCreature() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }
}
