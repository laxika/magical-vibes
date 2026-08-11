package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolcanicSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each player and each creature without flying")
    void damagesPlayersAndGroundCreatures() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new VolcanicSpray()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Normal cast puts Volcanic Spray into its owner's graveyard")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new VolcanicSpray()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Volcanic Spray");
    }

    @Test
    @DisplayName("Flashback deals damage and exiles Volcanic Spray after resolving")
    void flashbackDealsDamageAndExilesSpell() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setGraveyard(player1, List.of(new VolcanicSpray()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotInGraveyard(player1, "Volcanic Spray");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volcanic Spray"));
    }

    @Test
    @DisplayName("Flashback cannot be cast without enough mana")
    void flashbackRequiresTwoMana() {
        harness.setGraveyard(player1, List.of(new VolcanicSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
