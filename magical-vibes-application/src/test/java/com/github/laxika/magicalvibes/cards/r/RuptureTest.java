package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rupture.class, HillGiant.class, GrizzlyBears.class, FugitiveWizard.class,
        SuntailHawk.class, Ornithopter.class, GiantGrowth.class, Shock.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Hill Giant"));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Uses the creature's power before sacrificing it")
    void capturesPumpedPowerBeforeSacrifice() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new GiantGrowth(), new Rupture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        giveMana();

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, hillGiantId);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, hillGiantId);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A zero-power creature deals no damage")
    void zeroPowerCreatureDealsNoDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Rupture()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, 0);
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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A flying creature can be sacrificed, while other flyers take no damage")
    void canSacrificeFlyingCreature() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Rupture()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Suntail Hawk"));

        harness.assertInGraveyard(player1, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No sacrifice is paid on casting, and losing the only creature in response prevents damage")
    void creatureRemovedInResponseMeansNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Rupture(), new Shock()));
        giveMana();
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rupture");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
