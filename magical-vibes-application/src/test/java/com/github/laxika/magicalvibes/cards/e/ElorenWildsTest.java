package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElorenWilds.class, Forest.class, ParadiseDruid.class, SolRing.class, Shock.class})
class ElorenWildsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new ElorenWilds(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void givesAnAdditionalManaWhenAnyPlayerTapsALandForMana() {
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void givesAnAdditionalManaWhenAnyPlayerTapsANonlandPermanentForMana() {
        harness.addToBattlefield(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void givesAnAdditionalManaOfTheChosenTypeForAnAnyColorPermanent() {
        var druid = harness.addToBattlefieldAndReturn(player2, new ParadiseDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    void chaosPreventsTheTargetPlayerFromCastingUntilPlaneswalk() {
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isTrue();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.inMutationScope(() -> planar.planeswalk(gd));

        assertThat(harness.getCastingPermissionService()
                .isPlayerPreventedFromCasting(gd, player2.getId())).isFalse();
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
