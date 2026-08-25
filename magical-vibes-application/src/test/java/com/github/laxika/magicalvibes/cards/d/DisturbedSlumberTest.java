package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DisturbedSlumber.class, Forest.class, GrizzlyBears.class})
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

    private void castDisturbedSlumber(Permanent land) {
        harness.setHand(player1, List.of(new DisturbedSlumber()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
