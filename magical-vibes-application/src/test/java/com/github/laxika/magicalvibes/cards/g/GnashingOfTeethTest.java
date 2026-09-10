package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GnashingOfTeeth.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class})
class GnashingOfTeethTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives a creature -5/-5 and marks it for exile if it dies this turn")
    void firstModeDebuffsAndMarksCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The first mode exiles a creature killed by the reduction")
    void firstModeExilesCreatureKilledByReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The second mode affects only creatures controlled by the targeted player")
    void secondModeDebuffsTargetPlayersCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        Permanent targetPlayersCreature = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(8);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(8);
        assertThat(targetPlayersCreature.getEffectivePower()).isEqualTo(7);
        assertThat(targetPlayersCreature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Each mode enforces its target type")
    void modesRejectIllegalTargets() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first mode's debuff and exile marker last only until end of turn")
    void firstModeExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
        assertThat(target.isExileInsteadOfDieThisTurn()).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
