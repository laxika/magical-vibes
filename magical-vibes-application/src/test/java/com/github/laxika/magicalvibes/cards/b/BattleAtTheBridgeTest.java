package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FatalPush;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleAtTheBridge.class, FatalPush.class, GrizzlyBears.class, HillGiant.class,
        Swamp.class, WurmsTooth.class})
class BattleAtTheBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature -X/-X and the controller gains X life")
    void shrinksTargetAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Improvise taps an artifact to pay the generic mana")
    void improvisePaysGenericMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 1, target.getId(), null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal -X/-X puts the target creature into its graveyard")
    void lethalShrinkDestroysTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -X/-X wears off at the end of the turn")
    void penaltyWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Swamp");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("X can be zero and the spell can target your own creature")
    void zeroXCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertLife(player1, 20);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Battle at the Bridge");
    }

    @Test
    @DisplayName("No life is gained when the only target leaves before resolution")
    void illegalTargetPreventsLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.setHand(player2, List.of(new FatalPush()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Battle at the Bridge");
    }

    @Test
    @DisplayName("The same artifact cannot pay twice for improvise")
    void cannotCountSameArtifactTwice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 2, target.getId(), null,
                List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Improvise cannot pay the black mana requirement")
    void improviseCannotPayColoredMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, target.getId(), null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped artifact cannot pay for improvise")
    void cannotImproviseWithTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        artifact.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BattleAtTheBridge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, target.getId(), null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
