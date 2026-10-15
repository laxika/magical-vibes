package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoilingVortex;
import com.github.laxika.magicalvibes.cards.s.SpareSupplies;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnduInversion.class, OnduSkyruins.class, Forest.class, GrizzlyBears.class,
        RoilingVortex.class, SpareSupplies.class, TazeemRoilmage.class})
class OnduInversionTest extends BaseCardTest {

    @Test
    @DisplayName("Ondu Inversion destroys nonland permanents and preserves lands")
    void destroysNonlandPermanentsAndPreservesLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new OnduInversion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Ondu Skyruins enters tapped when its land face is played")
    void landFaceEntersTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OnduInversion()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(OnduSkyruins.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ondu Skyruins produces white mana")
    void landFaceProducesWhiteMana() {
        harness.addToBattlefield(player1, new OnduSkyruins());
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ondu Inversion destroys artifacts, enchantments, and creatures on both battlefields")
    void destroysDifferentNonlandPermanentTypes() {
        harness.addToBattlefield(player1, new SpareSupplies());
        harness.addToBattlefield(player2, new RoilingVortex());
        harness.addToBattlefield(player1, new TazeemRoilmage());
        harness.addToBattlefield(player2, new TazeemRoilmage());
        harness.setHand(player1, List.of(new OnduInversion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spare Supplies");
        harness.assertNotOnBattlefield(player2, "Roiling Vortex");
        harness.assertNotOnBattlefield(player1, "Tazeem Roilmage");
        harness.assertNotOnBattlefield(player2, "Tazeem Roilmage");
        harness.assertInGraveyard(player1, "Spare Supplies");
        harness.assertInGraveyard(player2, "Roiling Vortex");
        harness.assertInGraveyard(player1, "Tazeem Roilmage");
        harness.assertInGraveyard(player2, "Tazeem Roilmage");
        harness.assertInGraveyard(player1, "Ondu Inversion");
    }

    @Test
    @DisplayName("Ondu Inversion can resolve without any nonland permanents")
    void resolvesWithOnlyLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OnduInversion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Ondu Inversion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A played Ondu Skyruins survives Ondu Inversion")
    void preservesPlayedLandFace() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OnduInversion(), new OnduInversion()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> gs.playCard(gd, player1, 0, 1, null, null));
        harness.addToBattlefield(player2, new TazeemRoilmage());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ondu Skyruins");
        harness.assertNotInGraveyard(player1, "Ondu Skyruins");
        harness.assertInGraveyard(player1, "Ondu Inversion");
        harness.assertInGraveyard(player2, "Tazeem Roilmage");
    }

    @Test
    @DisplayName("Ondu Skyruins is played without mana or a stack entry and uses the land play")
    void landFaceUsesLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OnduInversion(), new Forest()));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> gs.playCard(gd, player1, 0, 1, null, null));

        harness.assertOnBattlefield(player1, "Ondu Skyruins");
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
