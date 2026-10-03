package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterwindScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisturbedSlumber.class, Forest.class, GrizzlyBears.class, ArmoredKincaller.class, WaterwindScout.class})
class DisturbedSlumberTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a land you control and makes it must be blocked")
    void animatesLandAndRequiresBlock() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castDisturbedSlumber(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, land, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-be-blocked land must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castDisturbedSlumber(land);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        declareLandAsAttacker(land);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(land)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Animation and must-be-blocked requirement wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castDisturbedSlumber(land);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DisturbedSlumber()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land you control");
    }

    @Test
    @DisplayName("Cannot evade the blocking requirement by blocking another attacker")
    void cannotAssignOnlyBlockerToAnotherAttacker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherAttacker = readyCreature(player1, new ArmoredKincaller());
        Permanent blocker = readyCreature(player2, new ArmoredKincaller());
        castDisturbedSlumber(land);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(land),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only one creature needs to block the animated land")
    void oneBlockerSatisfiesRequirementWithOtherBlockersAvailable() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blocker = readyCreature(player2, new ArmoredKincaller());
        readyCreature(player2, new ArmoredKincaller());
        castDisturbedSlumber(land);
        declareLandAsAttacker(land);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(land)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped creature does not force the land to be blocked")
    void noBlockRequiredWhenOnlyPotentialBlockerIsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blocker = readyCreature(player2, new ArmoredKincaller());
        blocker.setTapped(true);
        castDisturbedSlumber(land);
        declareLandAsAttacker(land);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Reach allows the animated land to block a flying creature")
    void animatedLandCanBlockFlyingCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent flyer = readyCreature(player2, new WaterwindScout());
        castDisturbedSlumber(land);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(flyer)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(land),
                gd.playerBattlefields.get(player2.getId()).indexOf(flyer)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A land that changes controller before resolution is an illegal target")
    void targetChangingControllerBeforeResolutionIsNotAnimated() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DisturbedSlumber()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.isMustBeBlockedThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Disturbed Slumber");
    }

    private void castDisturbedSlumber(Permanent land) {
        harness.setHand(player1, List.of(new DisturbedSlumber()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, land.getId());
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareLandAsAttacker(Permanent land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(land)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
