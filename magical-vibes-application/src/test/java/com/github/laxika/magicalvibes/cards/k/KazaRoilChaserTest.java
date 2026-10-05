package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.r.RoilEruption;
import com.github.laxika.magicalvibes.cards.s.SeaGateStormcaller;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazaRoilChaser.class, ZuranSpellcaster.class, Divination.class, GrizzlyBears.class,
        IntoTheRoil.class, RoilEruption.class, SeaGateStormcaller.class})
class KazaRoilChaserTest extends BaseCardTest {

    @Test
    void reducesTheNextInstantOrSorceryByTheNumberOfWizards() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    void doesNotReduceCreatureSpells() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesAnInstantAndConsumesTheReductionOnCasting() {
        var kaza = addCreatureReady(player1, new KazaRoilChaser());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new IntoTheRoil(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, kaza.getId());

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, kaza.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kaza, Roil Chaser");
    }

    @Test
    void cannotReduceColoredManaAndFailedCastDoesNotConsumeReduction() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new SeaGateStormcaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void countsWizardsAtResolutionAfterKazaLeavesInResponse() {
        var kaza = addCreatureReady(player1, new KazaRoilChaser());
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, kaza.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void keepsResolvedReductionAfterKazaLeaves() {
        var kaza = addCreatureReady(player1, new KazaRoilChaser());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, kaza.getId());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void doesNotCountOpponentsWizards() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player2, new SeaGateStormcaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void reducesGenericKickerCostAndDoesNotRecountWizardsAfterResolution() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new SeaGateStormcaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addCreatureReady(player1, new SeaGateStormcaller());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void reductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new KazaRoilChaser());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new RoilEruption(), new RoilEruption()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Kaza, Roil Chaser")))
                .isInstanceOf(IllegalStateException.class);
    }
}
