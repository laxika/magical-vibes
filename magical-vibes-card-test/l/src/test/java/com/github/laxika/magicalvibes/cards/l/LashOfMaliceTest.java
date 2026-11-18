package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LashOfMalice.class, CentaurCourser.class, FountainOfYouth.class})
class LashOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/-2 until end of turn")
    void givesTargetCreaturePlusTwoMinusTwo() {
        Permanent target = addCreature(player2);

        castLashOfMalice(target);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2);

        castLashOfMalice(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new LashOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        Permanent target = addCreature(player1);

        castLashOfMalice(target);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(-2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Repeated toughness reductions put the creature into its owner's graveyard")
    void repeatedReductionsKillCreature() {
        Permanent target = addCreature(player2);

        castLashOfMalice(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        castLashOfMalice(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not affect another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addCreature(player2);
        Permanent other = addCreature(player2);
        harness.setHand(player1, List.of(new LashOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lash of Malice");
    }

    private void castLashOfMalice(Permanent target) {
        harness.setHand(player1, List.of(new LashOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CentaurCourser());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
