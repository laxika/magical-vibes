package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.FeloniousRage;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SlimyDualleech.class, GrizzlyBears.class, HillGiant.class, FeloniousRage.class})
class SlimyDualleechTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Beginning of combat gives a qualifying creature +1/+0 and deathtouch")
    void beginningOfCombatBuffsQualifyingCreature() {
        harness.addToBattlefield(player1, new SlimyDualleech());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The boost and deathtouch wear off at end of turn")
    void buffsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SlimyDualleech());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you do not control or one with power greater than two")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new SlimyDualleech());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slimy Dualleech can target itself and keeps deathtouch after its power increases")
    void canTargetItself() {
        Permanent leech = harness.addToBattlefieldAndReturn(player1, new SlimyDualleech());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, leech.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, leech, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent leech = harness.addToBattlefieldAndReturn(player1, new SlimyDualleech());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, leech, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A target whose power rises above two before resolution receives neither effect")
    void targetPowerIsRecheckedOnResolution() {
        Permanent leech = harness.addToBattlefieldAndReturn(player1, new SlimyDualleech());
        harness.setHand(player1, List.of(new FeloniousRage()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, leech.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, leech.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, leech, Keyword.DEATHTOUCH)).isFalse();
    }
}
