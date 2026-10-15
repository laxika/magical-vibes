package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LevitatingStatue;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManFuturistParagon.class, GrizzlyBears.class, LevitatingStatue.class, Forest.class,
        TurnToFrog.class})
class IronManFuturistParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat animates a target artifact or creature permanently")
    void animatesTargetArtifactOrCreaturePermanently() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManFuturistParagon());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new LevitatingStatue());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                ironMan.getId(), targetCreature.getId(), targetArtifact.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, targetCreature)).isTrue();
        assertThat(gqs.isArtifact(targetCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The animation remains after cleanup and cannot target a land")
    void animationIsPermanentAndLandIsIllegalTarget() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManFuturistParagon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId(), ironMan.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animates an opponent's noncreature artifact and preserves its counters")
    void animatesOpponentsArtifactWithCounters() {
        harness.addToBattlefield(player1, new IronManFuturistParagon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LevitatingStatue());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not trigger at the beginning of an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new IronManFuturistParagon());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Grants flying after an earlier effect removed all abilities")
    void grantsFlyingAfterTurnToFrog() {
        harness.addToBattlefield(player1, new IronManFuturistParagon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, java.util.List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
