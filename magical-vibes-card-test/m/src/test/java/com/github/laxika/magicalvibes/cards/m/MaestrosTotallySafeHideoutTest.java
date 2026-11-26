package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.g.GlimpseTheUnthinkablePlaytest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaestrosTotallySafeHideout.class, GrizzlyBears.class, Memnite.class, GlimpseTheUnthinkablePlaytest.class, EsixFractalBloom.class})
class MaestrosTotallySafeHideoutTest extends BaseCardTest {

    @Test
    void payingLandCasualtySacrificesCreatureAndCreatesTappedCopy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(2);
        assertThat(findPermanents(player1, "Maestros' Totally Safe Hideout"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    void decliningLandCasualtyLetsOriginalLandEnterWithoutCopy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
        assertThat(findPermanent(player1, "Maestros' Totally Safe Hideout").isTapped()).isTrue();
    }

    @Test
    void creatureWithPowerOneCannotPayLandCasualty() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
    }

    @Test
    void manaAbilityAddsChosenBlueBlackOrRedMana() {
        harness.addToBattlefield(player1, new MaestrosTotallySafeHideout());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void landCasualtyCreatesCopyDuringEntryWithoutUsingTheStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(2);
        assertThat(findPermanents(player1, "Maestros' Totally Safe Hideout"))
                .allMatch(Permanent::isTapped);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noCreaturesLetsLandEnterTappedWithoutCopyOrPrompt() {
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
        assertThat(findPermanent(player1, "Maestros' Totally Safe Hideout").isTapped()).isTrue();
    }

    @Test
    void opponentsCreatureCannotPayLandCasualty() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
        assertThat(findPermanent(player1, "Maestros' Totally Safe Hideout").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    void manaAbilityAddsChosenBlackOrRedMana(ManaColor chosenColor) {
        harness.addToBattlefield(player1, new MaestrosTotallySafeHideout());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, chosenColor.name());

        for (ManaColor color : List.of(ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 1 : 0);
        }
        assertThat(findPermanent(player1, "Maestros' Totally Safe Hideout").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature that can't be chosen can't be sacrificed for land casualty")
    void unchoosableCreatureCannotPayLandCasualty() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
    }

    @Test
    @DisplayName("Esix is offered for the land casualty copy; accepted, the chosen creature is copied instead")
    void esixReplacesTheLandCasualtyCopy() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, memnite.getId());

        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Memnite"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining Esix for the land casualty copy creates the copy of the land")
    void decliningEsixCreatesTheLandCasualtyCopy() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new MaestrosTotallySafeHideout()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        // The copy has land casualty too; Esix could pay it, so its controller declines.
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(countPermanents(player1, "Maestros' Totally Safe Hideout")).isEqualTo(2);
        assertThat(findPermanents(player1, "Maestros' Totally Safe Hideout")).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
