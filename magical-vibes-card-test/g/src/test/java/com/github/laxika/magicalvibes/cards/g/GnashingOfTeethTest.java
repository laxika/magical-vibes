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

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.exilesInsteadOfDying(target.getId())).isTrue();
    }

    @Test
    @DisplayName("The first mode exiles a creature killed by the reduction")
    void firstModeExilesCreatureKilledByReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));

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

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, targetPlayersCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, targetPlayersCreature)).isEqualTo(7);
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

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gd.exilesInsteadOfDying(target.getId())).isFalse();
    }

    @Test
    @DisplayName("A creature surviving the first mode is exiled if it dies later that turn")
    void firstModeExilesSurvivorDyingLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        target.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("The second mode can target its controller and does not mark creatures for exile")
    void secondModeCanTargetControllerAndCreaturesDieNormally() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GnashingOfTeeth(), new GnashingOfTeeth()));
        addMana();
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player1.getId());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);

        harness.castAndResolveSorcery(player1, 0, 1, player1.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode affects creatures present at resolution and expires at cleanup")
    void secondModeDoesNotAffectLaterCreaturesAndExpires() {
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GnashingOfTeeth()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());
        Permanent later = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
