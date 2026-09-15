package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwlStoicStrategist.class, ProwlPursuitVehicle.class, GrizzlyBears.class, Mountain.class})
class ProwlStoicStrategistTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsProwlConverted() {
        Permanent prowl = castProwlConverted();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(prowl.getCard()).isInstanceOf(ProwlPursuitVehicle.class);
        assertThat(gqs.isCreature(gd, prowl)).isTrue();
        assertThat(gqs.isArtifact(gd, prowl)).isTrue();
    }

    @Test
    void anotherCreatureEntersPutsCounterAndSecondResolutionTransformsBack() {
        Permanent prowl = castProwlConverted();

        castBear();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(prowl.isTransformed()).isTrue();

        castBear();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(prowl.isTransformed()).isFalse();
    }

    @Test
    void attackExilesTappedPermanentWithPermissionAndPlayingItDrawsAndTransforms() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        ExiledCardEntry exiled = gd.findExiledCard(target.getCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(prowl.getId());
        assertThat(gd.exilePlayPermissions.get(target.getCard().getId())).isEqualTo(player2.getId());

        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, target.getCard().getId());
        harness.passBothPriorities();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent castProwlConverted() {
        harness.setHand(player1, List.of(new ProwlStoicStrategist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Prowl, Pursuit Vehicle");
    }

    private void castBear() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
