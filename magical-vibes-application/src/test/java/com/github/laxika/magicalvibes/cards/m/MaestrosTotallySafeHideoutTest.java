package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaestrosTotallySafeHideout.class, GrizzlyBears.class, Memnite.class})
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
}
