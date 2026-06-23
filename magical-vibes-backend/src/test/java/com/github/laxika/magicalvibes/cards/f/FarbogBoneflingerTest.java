package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FarbogBoneflingerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature -2/-2")
    void etbGivesMinusTwoMinusTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB — -2/-2 kills Bears

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell — ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target and enters battlefield normally")
    void canCastWithoutTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FarbogBoneflinger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Farbog Boneflinger"));
        assertThat(gd.stack).isEmpty();
    }
}
