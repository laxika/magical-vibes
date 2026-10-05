package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BastionOfRemembrance;
import com.github.laxika.magicalvibes.cards.c.CrystallineGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LukkaCoppercoatOutcast;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.cards.s.SleeperDart;
import com.github.laxika.magicalvibes.cards.s.SpringjawTrap;
import com.github.laxika.magicalvibes.cards.v.VivienMonstersAdvocate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythosOfSnapdax.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        BastionOfRemembrance.class, CrystallineGiant.class, LukkaCoppercoatOutcast.class,
        SeatOfTheSynod.class, SleeperDart.class, SpringjawTrap.class, VivienMonstersAdvocate.class})
class MythosOfSnapdaxTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses their own permanents when black and red mana were not spent")
    void eachPlayerChoosesTheirOwnPermanents() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castWithMana(ManaColor.COLORLESS, ManaColor.COLORLESS);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentGiant.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The controller chooses for every player when black and red mana were spent")
    void controllerChoosesForEveryPlayerWithBlackAndRedMana() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castWithMana(ManaColor.BLACK, ManaColor.RED);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentGiant.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    void spendingOnlyOneBonusColorDoesNotLetControllerChoose(ManaColor color) {
        Permanent dart = harness.addToBattlefieldAndReturn(player2, new SleeperDart());
        harness.addToBattlefield(player2, new SpringjawTrap());

        castWithMana(color, ManaColor.COLORLESS);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(dart.getId()));
        harness.assertOnBattlefield(player2, "Sleeper Dart");
        harness.assertInGraveyard(player2, "Springjaw Trap");
    }

    @Test
    void keepsOneOfEveryNonlandTypeAndWaitsUntilAllChoicesToSacrifice() {
        Permanent dart = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        harness.addToBattlefield(player1, new SpringjawTrap());
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new BastionOfRemembrance());
        Permanent otherBastion = harness.addToBattlefieldAndReturn(player1, new BastionOfRemembrance());
        harness.addToBattlefield(player1, new CrystallineGiant());
        Permanent lukka = harness.addToBattlefieldAndReturn(player1, new LukkaCoppercoatOutcast());
        lukka.setCounterCount(CounterType.LOYALTY, 5);
        Permanent vivien = harness.addToBattlefieldAndReturn(player1, new VivienMonstersAdvocate());
        vivien.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opponentDart = harness.addToBattlefieldAndReturn(player2, new SleeperDart());
        harness.addToBattlefield(player2, new SpringjawTrap());

        castWithMana(ManaColor.COLORLESS, ManaColor.COLORLESS);
        harness.handleMultiplePermanentsChosen(player1, List.of(dart.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bastion.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(vivien.getId()));

        harness.assertOnBattlefield(player1, "Springjaw Trap");
        harness.assertOnBattlefield(player1, "Lukka, Coppercoat Outcast");
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentDart.getId()));

        harness.assertOnBattlefield(player1, "Sleeper Dart");
        harness.assertOnBattlefield(player1, "Crystalline Giant");
        harness.assertOnBattlefield(player1, "Vivien, Monsters' Advocate");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(bastion.getId()).doesNotContain(otherBastion.getId());
        harness.assertInGraveyard(player1, "Springjaw Trap");
        harness.assertInGraveyard(player1, "Lukka, Coppercoat Outcast");
        harness.assertInGraveyard(player2, "Springjaw Trap");
    }

    @Test
    void sameArtifactCreatureCanBeKeptForBothTypes() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant());
        harness.addToBattlefield(player2, new SpringjawTrap());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castWithMana(ManaColor.BLACK, ManaColor.RED);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        harness.assertOnBattlefield(player2, "Crystalline Giant");
        harness.assertInGraveyard(player2, "Springjaw Trap");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void artifactLandCannotBeChosenAndOnlyNonlandArtifactIsAutomaticallyKept() {
        harness.addToBattlefield(player2, new SeatOfTheSynod());
        harness.addToBattlefield(player2, new SpringjawTrap());

        castWithMana(ManaColor.BLACK, ManaColor.RED);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Seat of the Synod");
        harness.assertOnBattlefield(player2, "Springjaw Trap");
        harness.assertNotInGraveyard(player2, "Springjaw Trap");
    }

    @Test
    void resolvesWithNoNonlandPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castWithMana(ManaColor.COLORLESS, ManaColor.COLORLESS);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Mythos of Snapdax");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    private void castWithMana(ManaColor firstExtraColor, ManaColor secondExtraColor) {
        harness.setHand(player1, List.of(new MythosOfSnapdax()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, firstExtraColor, 1);
        harness.addMana(player1, secondExtraColor, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
