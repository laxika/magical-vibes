package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightExemplar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZhalfirinCommanderTest extends BaseCardTest {

    private void addCommander() {
        harness.addToBattlefield(player1, new ZhalfirinCommander());
        findPermanent(player1, "Zhalfirin Commander").setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 3);
    }

    @Test
    @DisplayName("Ability gives target Knight +1/+1 until end of turn")
    void boostsTargetKnight() {
        addCommander();
        harness.addToBattlefield(player1, new KnightExemplar());

        UUID targetId = harness.getPermanentId(player1, "Knight Exemplar");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight Exemplar");
        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCommander();
        harness.addToBattlefield(player1, new KnightExemplar());

        UUID targetId = harness.getPermanentId(player1, "Knight Exemplar");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight Exemplar");
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot target a non-Knight creature")
    void rejectsNonKnightTarget() {
        addCommander();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
