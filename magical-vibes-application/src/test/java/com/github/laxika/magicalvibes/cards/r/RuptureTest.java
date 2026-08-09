package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuptureTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Sacrifices a creature and deals its power to ground creatures and each player")
    void sacrificesCreatureAndDealsPowerDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Rupture()));
        giveMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Hill Giant"));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Captures the sacrificed creature's power before sacrificing it")
    void zeroPowerCreatureDealsNoDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Rupture()));
        giveMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does nothing when the controller has no creature to sacrifice")
    void noCreatureMeansNoDamage() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Rupture()));
        giveMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
