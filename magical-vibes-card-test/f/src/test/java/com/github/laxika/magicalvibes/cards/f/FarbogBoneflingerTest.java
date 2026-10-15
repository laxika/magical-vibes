package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FarbogBoneflinger.class, DawntreaderElk.class, HeadlessSkaab.class})
class FarbogBoneflingerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature -2/-2")
    void etbGivesMinusTwoMinusTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HeadlessSkaab());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Headless Skaab");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell (ETB trigger on stack)
        harness.passBothPriorities(); // resolve ETB

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(giant.getPowerModifier()).isEqualTo(-2);
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB -2/-2 kills a 2/2 creature")
    void etbKillsSmallCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB — -2/-2 kills Bears

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell — ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering alone requires targeting itself")
    void enteringAloneMustTargetItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FarbogBoneflinger(), "{4}{B}");

        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Farbog Boneflinger");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Farbog Boneflinger"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Farbog Boneflinger");
        harness.assertInGraveyard(player1, "Farbog Boneflinger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Own creature can be targeted and the reduction ends at cleanup")
    void ownCreatureRecoversAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HeadlessSkaab());
        harness.castFromHand(player1, new FarbogBoneflinger(), "{4}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("ETB resolves even if Boneflinger leaves before resolution")
    void triggerSurvivesSourceLeaving() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        harness.castFromHand(player1, new FarbogBoneflinger(), "{4}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
