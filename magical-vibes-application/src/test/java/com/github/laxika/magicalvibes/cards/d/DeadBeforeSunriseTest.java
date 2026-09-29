package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadBeforeSunrise.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
class DeadBeforeSunriseTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your outlaws and grants them power-based creature damage")
    void boostsOutlawsAndGrantsDamageAbility() {
        Permanent outlaw = addOutlaw(player1);
        Permanent nonOutlaw = addCreatureReady(player1, new GrizzlyBears());
        addOutlaw(player2);
        Permanent target = addCreatureReady(player2, new HillGiant());

        castDeadBeforeSunrise();

        assertThat(outlaw.getEffectivePower()).isEqualTo(3);
        assertThat(nonOutlaw.getEffectivePower()).isEqualTo(2);

        int outlawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(outlaw);
        harness.activateAbility(player1, outlawIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The boost and granted ability last only until end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent outlaw = addOutlaw(player1);
        Permanent target = addCreatureReady(player2, new HillGiant());

        castDeadBeforeSunrise();
        assertThat(outlaw.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outlaw.getEffectivePower()).isEqualTo(2);
        int outlawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(outlaw);
        assertThatThrownBy(() -> harness.activateAbility(player1, outlawIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreature() {
        Permanent outlaw = addOutlaw(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castDeadBeforeSunrise();

        int outlawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(outlaw);
        assertThatThrownBy(() -> harness.activateAbility(player1, outlawIndex, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castDeadBeforeSunrise() {
        harness.setHand(player1, List.of(new DeadBeforeSunrise()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addOutlaw(Player player) {
        Permanent outlaw = addCreatureReady(player, new GrizzlyBears());
        TestCards.mutableCard(outlaw).setSubtypes(List.of(CardSubtype.ASSASSIN));
        return outlaw;
    }
}
