package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.b.Bookwurm;
import com.github.laxika.magicalvibes.cards.c.CrackleWithPower;
import com.github.laxika.magicalvibes.cards.e.ElementalMasterpiece;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.cards.p.PracticalResearch;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectacleMage.class, Concentrate.class, GrizzlyBears.class, MurderousCut.class,
        TimeWarp.class, Bookwurm.class, CrackleWithPower.class, ElementalMasterpiece.class,
        PracticalResearch.class})
class SpectacleMageTest extends BaseCardTest {

    @Test
    void reducesHighManaValueSorceryCost() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesHighManaValueInstantCost() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceLowerManaValueSpells() {
        harness.addToBattlefield(player1, new SpectacleMage());
        assertThatThrownBy(() -> harness.castFromHand(player1, new Concentrate(), "{1}{U}{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player2, List.of(new TimeWarp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsEachChosenXInManaValue() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Crackle with Power");
    }

    @Test
    void xZeroDoesNotQualifyOrReduceColoredRequirements() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Crackle with Power");
        harness.assertLife(player2, 20);
    }

    @Test
    void reductionsFromMultipleMagesStackAtManaValueFive() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.addToBattlefield(player1, new SpectacleMage());

        harness.castFromHand(player1, new PracticalResearch(), "{1}{U}{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesSpellsAboveManaValueFive() {
        harness.addToBattlefield(player1, new SpectacleMage());

        harness.castFromHand(player1, new ElementalMasterpiece(), "{4}{U}{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotReplaceColoredManaWithGenericMana() {
        harness.addToBattlefield(player1, new SpectacleMage());

        assertThatThrownBy(() -> harness.castFromHand(player1, new PracticalResearch(), "{3}{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceHighManaValueCreatureSpells() {
        harness.addToBattlefield(player1, new SpectacleMage());

        assertThatThrownBy(() -> harness.castFromHand(player1, new Bookwurm(), "{6}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
