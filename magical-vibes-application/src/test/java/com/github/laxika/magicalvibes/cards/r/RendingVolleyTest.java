package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendingVolley.class, Cancel.class, EliteVanguard.class, FugitiveWizard.class, GrizzlyBears.class})
class RendingVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a white creature")
    void dealsDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 4 damage to a blue creature")
    void dealsDamageToBlueCreature() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fugitive Wizard"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a creature that is not white or blue")
    void cannotTargetNonWhiteOrBlueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new EliteVanguard());
        RendingVolley rendingVolley = new RendingVolley();
        harness.setHand(player1, List.of(rendingVolley));
        addRendingVolleyMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rendingVolley.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void addRendingVolleyMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
