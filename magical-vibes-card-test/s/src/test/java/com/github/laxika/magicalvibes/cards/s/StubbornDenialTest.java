package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StubbornDenial.class, CrawWurm.class, GrizzlyBears.class, LlanowarElves.class, MightOfOaks.class, AlpineGrizzly.class})
class StubbornDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a noncreature spell when its controller cannot pay")
    void countersWhenControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Counters a noncreature spell unless its controller pays {1}")
    void countersUnlessControllerPays() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Might of Oaks");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Ferocious counters the spell without allowing payment")
    void ferociousCountersInstead() {
        harness.addToBattlefield(player2, new CrawWurm());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    void controllerCanDeclinePaymentWithManaAvailable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Might of Oaks");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void exactlyFourPowerEnablesFerocious() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    void opponentsLargeCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new CrawWurm());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Craw Wurm"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @CardUsed({SkysovereignConsulFlagship.class})
    void uncrewedVehicleDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new SkysovereignConsulFlagship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new StubbornDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Might of Oaks");
    }

    @Test
    void ferociousUsesPowerWhenDenialResolves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        MightOfOaks targetSpell = new MightOfOaks();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new StubbornDenial(), new MightOfOaks()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Might of Oaks");
    }
}
