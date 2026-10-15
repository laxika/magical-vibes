package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplatterGoblin.class, GrizzlyBears.class, Shock.class})
class SplatterGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("When Splatter Goblin dies, an opponent's creature gets -1/-1 until end of turn")
    void deathTriggerShrinksOpponentsCreatureUntilEndOfTurn() {
        Permanent splatterGoblin = harness.addToBattlefieldAndReturn(player1, new SplatterGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroySplatterGoblin(splatterGoblin);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Death trigger only allows creatures controlled by an opponent")
    void deathTriggerTargetFilter() {
        Permanent splatterGoblin = harness.addToBattlefieldAndReturn(player1, new SplatterGoblin());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroySplatterGoblin(splatterGoblin);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Death trigger has no effect when only friendly creatures remain")
    void noLegalTargetDoesNotShrinkFriendlyCreature() {
        Permanent splatterGoblin = harness.addToBattlefieldAndReturn(player1, new SplatterGoblin());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroySplatterGoblin(splatterGoblin);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Splatter Goblin");
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Death trigger does not affect another creature if its target dies in response")
    void targetDiesBeforeTriggerResolves() {
        Permanent splatterGoblin = harness.addToBattlefieldAndReturn(player1, new SplatterGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroySplatterGoblin(splatterGoblin);
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void destroySplatterGoblin(Permanent splatterGoblin) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, splatterGoblin.getId());
    }
}
